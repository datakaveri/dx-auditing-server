package org.cdpg.dx.database.postgres.models;

public class PaginationInfo {
  int page;
  int size;

  public PaginationInfo(int page, int size) {
    this.page = page;
    this.size = size;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
