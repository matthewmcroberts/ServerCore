package com.matthewmcroberts.modules.rank.client.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.matthewmcroberts.modules.rank.client.dto.RankDto;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankPermissionUpdateEvent {
    @NonNull
    RankDto updatedRankDto;

    @NonNull
    List<RankDto> updatedAffectedRankDtos;

    @NonNull Reason reason;

    public enum Reason {
        ADD_PERMISSION,
        REMOVE_PERMISSION
    }
}
