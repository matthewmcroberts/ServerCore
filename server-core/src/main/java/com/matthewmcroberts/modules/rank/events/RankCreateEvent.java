package com.matthewmcroberts.modules.rank.events;

import com.matthewmcroberts.modules.rank.models.Rank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a rank is created.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RankCreateEvent extends Event {
    /**
     * The handler list for this event.
     */
    @Getter
    @NonNull private static final HandlerList handlerList = new HandlerList();

    /**
     * The immutable instance of the rank that was created.
     */
    @NonNull private final Rank rank;

    /**
     * Constructs a new MineplexRankCreateEvent.
     *
     * @param rank   the rank being created
     */
    public RankCreateEvent(@NonNull final Rank rank) {
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
