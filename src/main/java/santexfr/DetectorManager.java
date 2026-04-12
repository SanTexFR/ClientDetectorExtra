package santexfr;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import santexfr.api.ClientDetectorAPI;
import santexfr.utils.DiscordWebhook;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class DetectorManager implements Listener{
    //VARIABLES(STATICS)
    private static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull Set<@NotNull Player>>clientCache=new ConcurrentHashMap<>();
    private static final @NotNull ConcurrentHashMap<UUID, String> playerToBrand = new ConcurrentHashMap<>();

    //METHODS(STATICS)
    public static@NotNull ConcurrentHashMap<@NotNull String,@NotNull Set<@NotNull Player>>getClientCache(){
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

    //METHODS(INSTANCES)
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final ClientDetectorExtra instance = ClientDetectorExtra.getInstance();
        final Player p = e.getPlayer();

        ClientDetectorExtra.getServerImplementation().entity(p).runDelayed(() -> {
            if (!p.isOnline()) return;

            final ClientDetectorAPI api = ClientDetectorExtra.getApi();
            final String brand = api.getClientBrand(p);
            final boolean bedrock = api.isBedrock(p);

            updateBrand(p, brand);

            ClientDetectorExtra.getServerImplementation().async().runNow(() -> {

                final String format = instance.getRawMessage("player-info");
                final String message = format
                        .replace("%player%", p.getName())
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
                            if (admin.hasPermission(perm)) admin.sendMessage(finalMessage);
                        }
                    });
                }

                handleWebhook(instance, p, brand, bedrock);
            });
        }, 3L);
    }

    private void handleWebhook(ClientDetectorExtra instance, Player p, String brand, boolean bedrock) {
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
                    .replace("%player%", p.getName())
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

        if (brand == null)return;

        Set<Player> players = clientCache.get(brand);
        if(players == null)return;

        players.remove(p);
        if (players.isEmpty()) {
            clientCache.remove(brand);
        }
    }
}