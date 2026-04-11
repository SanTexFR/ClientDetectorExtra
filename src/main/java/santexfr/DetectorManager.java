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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class DetectorManager implements Listener{
    //VARIABLES(STATICS)
    private static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull Set<@NotNull Player>>clientCache=new ConcurrentHashMap<>();

    //METHODS(STATICS)
    public static@NotNull ConcurrentHashMap<@NotNull String,@NotNull Set<@NotNull Player>>getClientCache(){
        return clientCache;
    }

    public static String getBrandOf(Player p) {
        for(Map.Entry<String, Set<Player>> entry : clientCache.entrySet()) {
            if(entry.getValue().contains(p)) return entry.getKey();
        }
        return null;
    }
    public static void updateBrand(Player p, String newBrand) {
        String old=getBrandOf(p);
        if(old != null) clientCache.get(old).remove(p);
        clientCache.computeIfAbsent(newBrand, k -> ConcurrentHashMap.newKeySet()).add(p);
    }

    //METHODS(INSTANCES)
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        final ClientDetectorExtra instance=ClientDetectorExtra.getInstance();

        final Player p=e.getPlayer();
        ClientDetectorExtra.getServerImplementation().entity(p).runDelayed(()->{
            if(!p.isOnline())return;
            final ClientDetectorAPI api=ClientDetectorExtra.getApi();

            final String brand=api.getClientBrand(p);
            clientCache.computeIfAbsent(brand,k->ConcurrentHashMap.newKeySet()).add(p);

            final boolean bedrock=api.isBedrock(p);

            final String format=instance.getRawMessage("player-info");

            final String message=format
                    .replace("%player%",p.getName())
                    .replace("%brand%",brand)
                    .replace("%is_bedrock%",bedrock?"Oui":"Non");

            final String finalMessage=instance.getPrefix()+message;

            if(instance.isNotifyAdmin()){
                final String perm=instance.getAdminPermission();
                for(final Player admin:Bukkit.getOnlinePlayers())
                    if(admin.hasPermission(perm))
                        admin.sendMessage(finalMessage);
            }

            if(instance.isNotifyConsole())
                Bukkit.getConsoleSender().sendMessage(finalMessage);

            // --- WEBHOOK DISCORD ---
            if (instance.isWebhookEnabled()){
                boolean shouldSend=false;

                if(instance.isWebhookAlertBedrock()&&bedrock){
                    shouldSend=true;
                }else if(instance.isWebhookAllClients()){
                    shouldSend=true;
                }else{
                    final List<String>brandsToAlert=instance.getWebhookAlertBrands();
                    for(String alertBrand:brandsToAlert){
                        if(brand.toLowerCase().contains(alertBrand.toLowerCase())){
                            shouldSend=true;
                            break;
                        }
                    }
                }

                if(shouldSend){
                    final String webhookFormat=instance.getRawMessage("webhook-message");

                    final String finalWebhookMessage=webhookFormat
                            .replace("%player%",p.getName())
                            .replace("%brand%",brand)
                            .replace("%is_bedrock%",bedrock?"Oui":"Non");

                    DiscordWebhook.send(instance.getWebhookUrl(),finalWebhookMessage);
                }
            }
        }, 3L);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        final Player p=e.getPlayer();

        for(Set<Player>players:clientCache.values())
            players.remove(p);

        clientCache.entrySet().removeIf(entry->entry.getValue().isEmpty());
    }
}