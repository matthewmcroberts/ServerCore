package com.matthewmcroberts.modules.rank.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankAssignment {
    private String playerId;

    @Setter
    private String rankId;
    private String assignedById;
}
