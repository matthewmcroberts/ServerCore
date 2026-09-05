package com.matthewmcroberts.modules.rank.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code PATCH /v1/ranks/{id}/priority}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityUpdate {
    private int priority;
}
