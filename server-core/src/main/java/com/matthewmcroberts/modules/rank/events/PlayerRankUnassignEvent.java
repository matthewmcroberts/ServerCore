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
 * Event triggered when a player's Mineplex rank is unassigned.
 * Contains information about the player and their unassigned rank.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PlayerRankUnassignEvent extends PlayerEvent {
    /**
     * The handler list for this event.
     */
    @Getter
    @NonNull private static final HandlerList handlerList = new HandlerList();

    /**
     * The immutable instance of the rank being unassigned.
     */
    @NonNull private final Rank rank;

    /**
     * Constructs a new MineplexPlayerRankUnassignEvent.
     *
     * @param player  the player whose rank is being unassigned
     * @param oldRank the unassigned rank
     */
    public PlayerRankUnassignEvent(
            @NonNull final Player player, @NonNull final Rank oldRank) {
        super(player);
        this.rank = oldRank;
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
