package com.matthewmcroberts.modules.rank.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankAssignmentDto {
    private String playerId;
    private String rankId;
    private String assignedById;
}
