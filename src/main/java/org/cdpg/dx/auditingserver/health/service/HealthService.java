package org.cdpg.dx.auditingserver.health.service;

import io.vertx.core.Future;

public interface HealthService {

  public Future<Void> checkLiveness();

  public Future<Void> checkReadiness();
}
