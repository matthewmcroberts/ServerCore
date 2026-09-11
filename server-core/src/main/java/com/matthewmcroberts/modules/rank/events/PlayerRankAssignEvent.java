package com.matthewmcroberts.modules.rank.events;

import com.matthewmcroberts.modules.rank.RankModule;
import com.matthewmcroberts.modules.rank.models.Rank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event triggered when a player's Mineplex rank is assigned.
 * Contains information about the player and their new rank.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlayerRankAssignEvent extends PlayerEvent {
    /**
     * The handler list for this event.
     */
    @Getter
    @NonNull private static final HandlerList handlerList = new HandlerList();

    /**
     * The immutable instance of the rank being assigned.
     */
    @NonNull private final Rank rank;

    /**
     * Constructs a new MineplexPlayerRankAssignEvent.
     *
     * @param player  the player whose rank is being assigned
     * @param rank the assigned rank
     */
    public PlayerRankAssignEvent(
            @NonNull final Player player, @NonNull final Rank rank) {
        super(player);
        this.rank = rank;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the handler list
     */
    @Override
    public @NonNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
