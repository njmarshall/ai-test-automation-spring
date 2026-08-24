package com.njmarshall.aitest.shared.crtp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * CRTP (Curiously Recurring Template Pattern) for a type-safe, fluent validator hierarchy.
 *
 * <p>The trick is the self type parameter {@code SELF extends AbstractValidator<T, SELF>}. Every
 * shared fluent method returns {@code self()}, which is typed as the concrete subclass, not the
 * base. So a chain that starts in the base and continues into a subclass keeps returning the
 * subclass, and subclass-specific methods stay reachable mid-chain with no casts:
 *
 * <pre>{@code
 * new PriorAuthValidator(pa)
 *     .expect(pa.id() != null, "id present")   // base method, returns PriorAuthValidator
 *     .hasStatus(APPROVED)                      // subclass method, still reachable
 *     .verify();
 * }</pre>
 *
 * <p>Without CRTP the base methods would return {@code AbstractValidator} and the chain would lose
 * the subclass type after the first base call. This is the same self-typing his Python framework
 * expressed with {@code Generic[T]} on the Pydantic model validators.
 *
 * @param <T>    the subject type being validated
 * @param <SELF> the concrete validator subclass
 */
public abstract class AbstractValidator<T, SELF extends AbstractValidator<T, SELF>> {

    protected final T subject;
    private final List<String> failures = new ArrayList<>();

    protected AbstractValidator(T subject) {
        this.subject = subject;
    }

    /** Subclasses return {@code this}. This is the whole mechanism behind CRTP self-typing. */
    protected abstract SELF self();

    public SELF expect(boolean condition, String description) {
        if (!condition) {
            failures.add(description);
        }
        return self();
    }

    public SELF expectThat(Predicate<T> predicate, String description) {
        return expect(predicate.test(subject), description);
    }

    public SELF expectEquals(Object actual, Object expected, String field) {
        return expect(
                java.util.Objects.equals(actual, expected),
                String.format("%s expected <%s> but was <%s>", field, expected, actual));
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }

    public boolean isValid() {
        return failures.isEmpty();
    }

    /** Throw with every collected failure if any check failed. Returns the subject when valid. */
    public T verify() {
        if (!failures.isEmpty()) {
            throw new ValidationException(getClass().getSimpleName(), failures);
        }
        return subject;
    }
}
