package com.matthewmcroberts.modules.rank.client.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.matthewmcroberts.modules.rank.client.dto.Rank;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankInheritanceUpdateEvent {
    @NonNull
    Rank updatedRank;

    @NonNull
    List<Rank> updatedAffectedRanks;

    @NonNull Reason reason;

    public enum Reason {
        ADD_INHERITANCE,
        REMOVE_INHERITANCE
    }
}
