package santexfr;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import santexfr.api.ClientDetectorAPI;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class DetectorManager implements Listener{

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        final ClientDetectorExtra instance=ClientDetectorExtra.getInstance();
        if(!instance.isNotifyAdmin()&&!instance.isNotifyConsole())return;

        final Player p=e.getPlayer();
        ClientDetectorExtra.getServerImplementation().entity(p).runDelayed(()->{
            final ClientDetectorAPI api=ClientDetectorExtra.getApi();

            final String brand=api.getClientBrand(p);
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
        }, 3L);
    }
}