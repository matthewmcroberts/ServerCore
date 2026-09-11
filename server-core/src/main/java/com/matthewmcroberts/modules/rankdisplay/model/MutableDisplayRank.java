package com.matthewmcroberts.modules.rankdisplay.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

@Data
@Builder
public class MutableDisplayRank implements DisplayRank {
    @NonNull private final String id;

    @NonNull private Component displayName;

    @Builder.Default
    private int priority = 1;
}
