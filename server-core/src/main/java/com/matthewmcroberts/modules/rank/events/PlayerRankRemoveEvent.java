package com.matthewmcroberts.modules.rank.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.matthewmcroberts.modules.rank.models.PlayerRankAssignment;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerRankRemoveEvent {
    @NonNull
    PlayerRankAssignment playerRankAssignment;
}
