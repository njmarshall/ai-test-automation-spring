package com.njmarshall.aitest.shared.client.poll;

/**
 * Decides whether a workflow state is terminal. Each domain supplies its own answer: for prior
 * auth, APPROVED and DENIED are terminal; for claims, PAID and REJECTED are. Because this is a
 * functional interface, a status enum can implement the check as a method reference.
 */
@FunctionalInterface
public interface TerminalStateResolver {

    boolean isTerminal(String state);
}
