package com.njmarshall.aitest.shared.crtp;

import java.util.List;

/** Aggregates every failed check from a validator into one readable message. */
public class ValidationException extends AssertionError {

    private final transient List<String> failures;

    public ValidationException(String validatorName, List<String> failures) {
        super(validatorName + " found " + failures.size() + " problem(s):\n  - "
                + String.join("\n  - ", failures));
        this.failures = List.copyOf(failures);
    }

    public List<String> failures() {
        return failures;
    }
}
