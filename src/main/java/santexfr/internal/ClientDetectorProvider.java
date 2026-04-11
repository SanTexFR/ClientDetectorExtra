package santexfr.internal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import santexfr.DetectorManager;
import santexfr.api.ClientDetectorAPI;

import java.util.*;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ClientDetectorProvider implements ClientDetectorAPI{
    public boolean isBedrock(@NotNull Player p){
        return p.getUniqueId().getMostSignificantBits()==0;
    }

    public@NotNull String getClientBrand(@NotNull Player p){
        final String brand=p.getClientBrandName();
        return(brand!=null)?brand:"Unknown";
    }

    public@NotNull List<@NotNull Player>getPlayersByClientBrand(@NotNull String brand){
        final Set<Player>players=DetectorManager.getClientCache().get(brand);
        return players==null?Collections.emptyList():new ArrayList<>(players);
    }

    public@NotNull Set<@NotNull String>getActiveClientBrands(){
        return DetectorManager.getClientCache().keySet();
    }

    public @NotNull String getBaseBrand(@NotNull String rawBrand) {
        String lower = rawBrand.toLowerCase();
        if (lower.contains("lunar")) return "Lunar Client";
        if (lower.contains("fabric")) return "Fabric";
        if (lower.contains("forge")) return "Forge";
        if (lower.contains("geyser") || lower.contains("bedrock")) return "Bedrock Edition";
        if (lower.contains("vanilla")) return "Vanilla";
        if (lower.contains("optifine")) return "OptiFine";
        if (lower.contains("labymod")) return "LabyMod";
        if (lower.contains("feather")) return "Feather Client";
        if (lower.contains("badlion")) return "Badlion Client";
        if (lower.contains("meteor")) return "Meteor Client";
        return rawBrand.split(":")[0];
    }

    public @NotNull Map<String, Integer> getGlobalStats() {
        Map<String, Integer> stats = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String raw = DetectorManager.getBrandOf(p);
            if (raw == null) raw = getClientBrand(p);
            String base = getBaseBrand(raw);
            stats.put(base, stats.getOrDefault(base, 0) + 1);
        }
        return stats;
    }
}