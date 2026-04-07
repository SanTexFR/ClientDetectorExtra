package santexfr.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface ClientDetectorAPI{
    boolean isBedrock(@NotNull Player p);

    @NotNull
    String getClientBrand(@NotNull Player p);
}