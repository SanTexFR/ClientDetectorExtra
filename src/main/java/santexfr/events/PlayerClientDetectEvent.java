package santexfr.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerClientDetectEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String clientBrand;
    private final boolean isBedrock;

    public PlayerClientDetectEvent(@NotNull Player player, @NotNull String clientBrand, boolean isBedrock) {
        this.player = player;
        this.clientBrand = clientBrand;
        this.isBedrock = isBedrock;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public String getClientBrand() {
        return clientBrand;
    }

    public boolean isBedrock() {
        return isBedrock;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}