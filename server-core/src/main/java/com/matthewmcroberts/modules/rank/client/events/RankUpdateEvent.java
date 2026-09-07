package com.matthewmcroberts.modules.rank.client.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.matthewmcroberts.modules.rank.client.dto.RankDto;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RankUpdateEvent {
    @NonNull
    RankDto rankDto;

    @NonNull Reason reason;

    public enum Reason {
        UPDATE_DISPLAY_NAME,
        UPDATE_PRIORITY
    }
}
