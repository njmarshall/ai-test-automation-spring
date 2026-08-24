package com.njmarshall.aitest.projects.healthcare.model;

/**
 * Prior authorization lifecycle. A request is received, sits in review, then settles into a
 * terminal decision. The terminal set is what {@code pollUntilTerminal} watches for.
 */
public enum PriorAuthStatus {

    RECEIVED(false),
    PENDING(false),
    IN_REVIEW(false),
    APPROVED(true),
    DENIED(true),
    CANCELLED(true);

    private final boolean terminal;

    PriorAuthStatus(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }

    /** True when the given state name maps to a terminal status. Unknown names are non-terminal. */
    public static boolean isTerminalState(String state) {
        try {
            return valueOf(state).isTerminal();
        } catch (IllegalArgumentException unknown) {
            return false;
        }
    }
}
