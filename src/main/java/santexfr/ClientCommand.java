package santexfr;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ClientCommand implements CommandExecutor{
    @Override
    public boolean onCommand(@NotNull CommandSender s,@NotNull Command command,@NotNull String label,@NotNull String[] args){
        final ClientDetectorExtra instance=ClientDetectorExtra.getInstance();

        if(!s.hasPermission(instance.getAdminPermission())){
            s.sendMessage(instance.getMessage("no-permission"));
            return true;
        }

        if(args.length>0 && (args[0].equalsIgnoreCase("stats"))) {
            java.util.Map<String, Integer> stats = ClientDetectorExtra.getApi().getGlobalStats();
            int total = Bukkit.getOnlinePlayers().size();

            s.sendMessage(instance.getPrefix() + "§8[§bStatistiques Globales§8]");
            if(total == 0) {
                s.sendMessage("§cAucun joueur en ligne.");
                return true;
            }

            for(java.util.Map.Entry<String, Integer> entry : stats.entrySet()) {
                double percent = (entry.getValue() * 100.0) / total;
                s.sendMessage("§7- §b" + entry.getKey() + "§f: " + entry.getValue() + " joueur(s) §8(§a" + String.format("%.1f", percent) + "%§8)");
            }
            return true;
        }

        if(args.length>0&&args[0].equalsIgnoreCase("reload")){
            instance.loadConfiguration();
            s.sendMessage(instance.getMessage("reload"));
            return true;
        }

        if(args.length>0&&(args[0].equalsIgnoreCase("gui")||args[0].equalsIgnoreCase("menu"))){
            if(s instanceof Player){
                ClientMenu.openMainMenu((Player) s,0);
            } else {
                s.sendMessage(instance.getPrefix() + "§cSeul un joueur peut ouvrir le menu.");
            }
            return true;
        }

        if(args.length>1&&(args[0].equalsIgnoreCase("info")||args[0].equalsIgnoreCase("check"))){
            final Player target=Bukkit.getPlayer(args[1]);
            if(target==null){
                s.sendMessage(instance.getMessage("player-not-found"));
                return true;
            }

            final String brand=ClientDetectorExtra.getApi().getClientBrand(target);
            final boolean bedrock=ClientDetectorExtra.getApi().isBedrock(target);

            final String processed=instance.getRawMessage("player-check")
                    .replace("%target%",target.getName())
                    .replace("%brand%",brand)
                    .replace("%is_bedrock%",bedrock?"Oui":"Non");

            s.sendMessage(instance.getPrefix()+processed);
            return true;
        }

        s.sendMessage("§b--- ClientDetectorExtra ---");
        s.sendMessage("§f/"+label+" info <joueur> §7- Voir le client d'un joueur");
        s.sendMessage("§f/"+label+" reload §7- Recharge la config");
        s.sendMessage("§f/"+label+" gui §7- Ouvre le menu des clients");
        s.sendMessage("§f/"+label+" stats §7- Affiche les pourcentages");
        return true;
    }
}