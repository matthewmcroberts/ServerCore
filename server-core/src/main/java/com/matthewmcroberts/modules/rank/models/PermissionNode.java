package com.matthewmcroberts.modules.rank.models;

import lombok.NonNull;

public final class PermissionNode {
    private static final String NEGATION_PREFIX = "-";

    @NonNull private final String node;

    private PermissionNode(@NonNull final String node) {
        this.node = node;
    }

    public static PermissionNode of(@NonNull final String node) {
        return new PermissionNode(node);
    }

    public static PermissionNode ofNegated(@NonNull final String node) {
        return new PermissionNode(NEGATION_PREFIX + node);
    }

    public String getValue() {
        return this.node;
    }

    @Override
    public String toString() {
        return this.node;
    }
}
