package org.cdpg.dx.auditingserver.health.service;

import io.vertx.core.Future;

public interface HealthService {

  Future<Void> checkLiveness();

  Future<Void> checkReadiness();
}
