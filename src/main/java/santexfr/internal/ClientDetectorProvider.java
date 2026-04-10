package santexfr.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import santexfr.DetectorManager;
import santexfr.api.ClientDetectorAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
}