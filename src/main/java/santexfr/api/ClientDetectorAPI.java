package santexfr.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface ClientDetectorAPI{
    boolean isBedrock(@NotNull Player p);

    @NotNull String getClientBrand(@NotNull Player p);

    @NotNull List< @NotNull Player>getPlayersByClientBrand(@NotNull String brand);
    @NotNull Set<String> getActiveClientBrands();
}