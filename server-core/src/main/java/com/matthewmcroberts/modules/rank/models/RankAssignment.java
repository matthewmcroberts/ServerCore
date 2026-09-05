package com.matthewmcroberts.modules.rank.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /v1/players/{playerId}/rank}.
 * {@code assignedById} is nullable — omit if the assignment has no audited assigner.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankAssignment {
    private String rankId;
    private String assignedById;
}
