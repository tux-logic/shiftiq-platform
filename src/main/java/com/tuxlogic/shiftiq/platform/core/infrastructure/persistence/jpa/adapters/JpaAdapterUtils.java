package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility helper to eliminate persistence entity resolution boilerplate across JPA adapters.
 */
public final class JpaAdapterUtils {

    private JpaAdapterUtils() {}

    /**
     * Resolves an existing persistence entity by ID or instantiates a new entity if ID is null or missing.
     */
    public static <E, ID> E resolveEntity(
            ID id,
            Function<ID, Optional<E>> findById,
            Supplier<E> entitySupplier) {
        if (id != null) {
            return findById.apply(id).orElseGet(entitySupplier);
        }
        return entitySupplier.get();
    }
}
