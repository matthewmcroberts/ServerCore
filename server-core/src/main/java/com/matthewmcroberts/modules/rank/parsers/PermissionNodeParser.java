package com.matthewmcroberts.modules.rank.parsers;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class PermissionNodeParser {
    private static final char NEGATION_PREFIX = '-';

    private final String rawNode;
    private final boolean negated;
    private final String sanitizedNode;

    /**
     * Creates a new parser instance for the given permission node.
     *
     * @param node the permission node to parse
     */
    public PermissionNodeParser(@NonNull final String node) {
        this.rawNode = node;
        this.negated = node.charAt(0) == NEGATION_PREFIX;
        this.sanitizedNode = this.negated ? node.substring(1) : node;
    }
}
