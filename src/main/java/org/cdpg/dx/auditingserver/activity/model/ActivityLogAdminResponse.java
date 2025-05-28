package org.cdpg.dx.auditingserver.activity.model;

import org.cdpg.dx.database.postgres.base.entity.BaseEntity;

import java.util.List;

public class ActivityLogAdminResponse<T extends BaseEntity<T>> {
  private final PaginationInfo pagination;
  private final List<T> result;

  public ActivityLogAdminResponse(Pagination<T> pagination) {
    this.pagination = new PaginationInfo(pagination);
    this.result = pagination.data();
  }

  public PaginationInfo getPagination() {
    return pagination;
  }

  public List<T> getResult() {
    return result;
  }

  public static class PaginationInfo {
    private final int page;
    private final int size;
    private final long totalCount;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PaginationInfo(Pagination<?> pagination) {
      this.page = pagination.page();
      this.size = pagination.size();
      this.totalCount = pagination.totalCount();
      this.totalPages = pagination.totalPages();
      this.hasNext = pagination.hasNext();
      this.hasPrevious = pagination.hasPrevious();
    }

    public int getPage() {
      return page;
    }

    public int getSize() {
      return size;
    }

    public long getTotalCount() {
      return totalCount;
    }

    public int getTotalPages() {
      return totalPages;
    }

    public boolean isHasNext() {
      return hasNext;
    }

    public boolean isHasPrevious() {
      return hasPrevious;
    }
  }
}
