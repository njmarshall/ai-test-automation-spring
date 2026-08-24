package com.njmarshall.aitest.shared.crtp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * CRTP base for a type-safe, fluent test-data factory hierarchy.
 *
 * <p>Each concrete factory starts from a valid-by-default entity and exposes fluent {@code withX}
 * setters. Those setters, plus the shared ones here, return {@code self()} typed as the concrete
 * subclass, so chains never lose the subtype. {@link #build()} produces one entity;
 * {@link #buildMany(int)} produces a batch, which is how the framework seeds bulk fixtures.
 *
 * @param <T>    the entity type produced
 * @param <SELF> the concrete factory subclass
 */
public abstract class AbstractEntityFactory<T, SELF extends AbstractEntityFactory<T, SELF>> {

    /** Applied to each freshly built entity, in order, before it is returned. */
    private final List<Consumer<T>> mutators = new ArrayList<>();

    protected abstract SELF self();

    /** Build one valid-by-default instance. Subclasses assemble the entity from their field state. */
    protected abstract T create();

    /**
     * Register an arbitrary post-build mutation. Useful for negative-path fixtures where a test
     * wants to invalidate one field of an otherwise valid entity.
     */
    public SELF with(Consumer<T> mutator) {
        mutators.add(mutator);
        return self();
    }

    /** Build one entity, applying any registered mutators. */
    public T build() {
        T entity = create();
        for (Consumer<T> mutator : mutators) {
            mutator.accept(entity);
        }
        return entity;
    }

    /** Build {@code count} entities from the current configuration. */
    public List<T> buildMany(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        List<T> batch = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            batch.add(build());
        }
        return batch;
    }
}
