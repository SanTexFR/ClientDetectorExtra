package santexfr.internal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import santexfr.ClientDetectorExtra;
import santexfr.DetectorManager;

import java.nio.charset.StandardCharsets;

public class BrandMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("minecraft:brand")) return;

        // Récupère la chaîne et nettoie les caractères invisibles de longueur
        String newBrand = new String(message, StandardCharsets.UTF_8).replaceAll("[^\\x20-\\x7E]", "");

        String cachedBrand = DetectorManager.getBrandOf(player);
        if (cachedBrand != null && !cachedBrand.equals(newBrand)) {
            DetectorManager.updateBrand(player, newBrand);

            String msg = ClientDetectorExtra.getInstance().getPrefix() + "§c[Alerte] §f" + player.getName() + " §fa changé de client en jeu ! §8(§7" + cachedBrand + " §8-> §b" + newBrand + "§8)";

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(ClientDetectorExtra.getInstance().getAdminPermission())) {
                    admin.sendMessage(msg);
                }
            }
            if (ClientDetectorExtra.getInstance().isNotifyConsole()) {
                Bukkit.getConsoleSender().sendMessage(msg);
            }
        }
    }
}