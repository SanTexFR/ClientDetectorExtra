package santexfr;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import santexfr.api.ClientDetectorAPI;
import santexfr.events.PlayerClientDetectEvent;
import santexfr.utils.DiscordWebhook;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class DetectorManager implements Listener{
    private static final @NotNull ConcurrentHashMap<@NotNull String,@NotNull Set<@NotNull Player>> clientCache = new ConcurrentHashMap<>();
    private static final @NotNull ConcurrentHashMap<UUID, String> playerToBrand = new ConcurrentHashMap<>();

    public static @NotNull ConcurrentHashMap<@NotNull String,@NotNull Set<@NotNull Player>> getClientCache(){
        return clientCache;
    }

    public static String getBrandOf(Player p) {
        return playerToBrand.get(p.getUniqueId());
    }

    public static void updateBrand(Player p, String newBrand) {
        UUID uuid = p.getUniqueId();
        String old = playerToBrand.get(uuid);

        if (old != null) {
            Set<Player> players = clientCache.get(old);
            if (players != null) players.remove(p);
        }

        playerToBrand.put(uuid, newBrand);
        clientCache.computeIfAbsent(newBrand, k -> ConcurrentHashMap.newKeySet()).add(p);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final ClientDetectorExtra instance = ClientDetectorExtra.getInstance();
        final Player p = e.getPlayer();

        ClientDetectorExtra.getServerImplementation().entity(p).runDelayed(() -> {
            if (!p.isOnline()) return;

            final String pName = p.getName();
            final ClientDetectorAPI api = ClientDetectorExtra.getApi();
            final String brand = api.getClientBrand(p);
            final boolean bedrock = api.isBedrock(p);

            updateBrand(p, brand);

            // 1. Déclencher l'événement personnalisé
            ClientDetectorExtra.getServerImplementation().global().execute(() -> {
                PlayerClientDetectEvent detectEvent = new PlayerClientDetectEvent(p, brand, bedrock);
                Bukkit.getPluginManager().callEvent(detectEvent);
            });

            // 2. Gestion de la Blacklist & Actions Automatiques
            checkBlacklistAndExecuteActions(instance, p, brand);

            ClientDetectorExtra.getServerImplementation().async().runNow(() -> {
                final String format = instance.getRawMessage("player-info");
                final String message = format
                        .replace("%player%", pName)
                        .replace("%brand%", brand)
                        .replace("%is_bedrock%", bedrock ? "Oui" : "Non");
                final String finalMessage = instance.getPrefix() + message;

                if (instance.isNotifyConsole()) {
                    Bukkit.getConsoleSender().sendMessage(finalMessage);
                }

                if (instance.isNotifyAdmin()) {
                    final String perm = instance.getAdminPermission();
                    ClientDetectorExtra.getServerImplementation().global().run(() -> {
                        for (Player admin : Bukkit.getOnlinePlayers()) {
                            if (admin.hasPermission(perm)) {
                                admin.sendMessage(finalMessage);
                            }
                        }
                    });
                }

                CompletableFuture.runAsync(() -> handleWebhook(instance, pName, brand, bedrock));
            });
        }, 3L);
    }

    private void checkBlacklistAndExecuteActions(ClientDetectorExtra instance, Player p, String brand) {
        if (!instance.getConfig().getBoolean("blacklist.enabled", false)) return;

        List<String> blacklistedKeywords = instance.getConfig().getStringList("blacklist.restricted-brands");
        boolean isRestricted = false;

        for (String keyword : blacklistedKeywords) {
            if (brand.toLowerCase().contains(keyword.toLowerCase())) {
                isRestricted = true;
                break;
            }
        }

        if (isRestricted) {
            List<String> actions = instance.getConfig().getStringList("blacklist.actions");
            for (String action : actions) {
                String[] parts = action.split(":", 2);
                String type = parts[0].toUpperCase();
                String value = parts.length > 1 ? parts[1].replace("%player%", p.getName()) : "";

                ClientDetectorExtra.getServerImplementation().global().run(() -> {
                    switch (type) {
                        case "KICK":
                            p.kick(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(value.isEmpty() ? "<red>Client non autorisé !" : value));
                            break;
                        case "BAN":
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + p.getName() + " " + value);
                            break;
                        case "COMMAND":
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                            break;
                        case "NOTIFY":
                            String alertMsg = instance.getPrefix() + "§c[Alerte Blacklist] §f" + p.getName() + " utilise un client interdit : §e" + brand;
                            for (Player admin : Bukkit.getOnlinePlayers()) {
                                if (admin.hasPermission(instance.getAdminPermission())) admin.sendMessage(alertMsg);
                            }
                            break;
                    }
                });
            }
        }
    }

    private void handleWebhook(ClientDetectorExtra instance, String name, String brand, boolean bedrock) {
        if (!instance.isWebhookEnabled()) return;

        boolean shouldSend = false;
        if (instance.isWebhookAlertBedrock() && bedrock) {
            shouldSend = true;
        } else if (instance.isWebhookAllClients()) {
            shouldSend = true;
        } else {
            List<String> brandsToAlert = instance.getWebhookAlertBrands();
            for (String alertBrand : brandsToAlert) {
                if (brand.toLowerCase().contains(alertBrand.toLowerCase())) {
                    shouldSend = true;
                    break;
                }
            }
        }

        if (shouldSend) {
            String webhookFormat = instance.getRawMessage("webhook-message");
            String finalWebhookMessage = webhookFormat
                    .replace("%player%", name)
                    .replace("%brand%", brand)
                    .replace("%is_bedrock%", bedrock ? "Oui" : "Non");

            DiscordWebhook.send(instance.getWebhookUrl(), finalWebhookMessage);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();
        String brand = playerToBrand.remove(uuid);

        if (brand == null) return;

        Set<Player> players = clientCache.get(brand);
        if (players == null) return;

        players.remove(p);
        if (players.isEmpty()) {
            clientCache.remove(brand);
        }
    }
}