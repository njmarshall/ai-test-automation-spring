package com.njmarshall.aitest.projects.insurance.model;

/** Insurance claim adjudication lifecycle. PAID, DENIED, and REJECTED are terminal. */
public enum ClaimStatus {

    SUBMITTED(false),
    ACCEPTED(false),
    IN_ADJUDICATION(false),
    PAID(true),
    DENIED(true),
    REJECTED(true);

    private final boolean terminal;

    ClaimStatus(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public static boolean isTerminalState(String state) {
        try {
            return valueOf(state).isTerminal();
        } catch (IllegalArgumentException unknown) {
            return false;
        }
    }
}
