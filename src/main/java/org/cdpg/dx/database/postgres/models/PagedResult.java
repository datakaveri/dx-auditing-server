package org.cdpg.dx.database.postgres.models;

import java.util.List;
import org.cdpg.dx.database.postgres.base.entity.BaseEntity;

public record PagedResult<T extends BaseEntity<T>>(
    int page,
    int size,
    long totalCount,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious,
    List<T> data) {}
