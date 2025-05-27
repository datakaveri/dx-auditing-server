package org.cdpg.dx.auditingserver.activity.model;

import java.util.List;
import org.cdpg.dx.database.postgres.base.entity.BaseEntity;

public record Pagination<T extends BaseEntity<T>>(
    int page,
    int size,
    long totalCount,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious,
    List<T> data) {}
