package santexfr.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import santexfr.api.ClientDetectorAPI;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ClientDetectorProvider implements ClientDetectorAPI{
    public boolean isBedrock(@NotNull Player p){
        return p.getUniqueId().getMostSignificantBits()==0;
    }

    public@NotNull String getClientBrand(@NotNull Player p){
        final String brand=p.getClientBrandName();
        return(brand!=null)?brand:"Unknown";
    }
}