package com.matthewmcroberts.modules.rank.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request body for {@code POST /v1/ranks/{id}/permissions}
 * and {@code DELETE /v1/ranks/{id}/permissions}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsUpdate {
    private Set<String> permissions;
}
