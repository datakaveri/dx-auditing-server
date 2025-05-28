package org.cdpg.dx.auditingserver.activity.model;

import java.util.UUID;

public class ActivityLogAdminRequest {
  private UUID userId;
  private String startTime;
  private String endTime;
  private int limit;
  private int offset;

  public ActivityLogAdminRequest(
      UUID userId, String startTime, String endTime, int limit, int offset) {
    this.userId = userId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.limit = limit;
    this.offset = offset;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }
}
