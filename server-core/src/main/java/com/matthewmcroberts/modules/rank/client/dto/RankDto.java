package com.matthewmcroberts.modules.rank.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Represents a rank resource returned from the API.
 * Used as the body for all rank read and write endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankDto {
    private String id;
    private String displayName;
    private int priority;
    private Set<String> ownPermissions;
    private Set<String> effectivePermissions;
    private Set<String> inheritedRankIds;
}
