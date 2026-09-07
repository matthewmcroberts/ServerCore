package com.matthewmcroberts.modules.rank.client.dto;

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
public class RankAssignmentDto {
    private String rankId;
    private String assignedById;
}
