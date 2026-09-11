package com.matthewmcroberts.modules.rank.events;

import com.matthewmcroberts.modules.rank.client.dto.Rank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a rank is deleted.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RankDeleteEvent extends Event {
    /**
     * The handler list for this event.
     */
    @Getter
    @NonNull private static final HandlerList handlerList = new HandlerList();

    /**
     * The old common model of the rank that was deleted.
     */
    @NonNull private final Rank deletedRank;

    /**
     * Constructs a new MineplexRankDeleteEvent.
     *
     * @param deletedRank   the rank that was deleted
     */
    public RankDeleteEvent(@NonNull final Rank deletedRank) {
        this.deletedRank = deletedRank;
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
