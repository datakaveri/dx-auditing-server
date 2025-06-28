package org.cdpg.dx.auditingserver.health.controller;

import org.cdpg.dx.auditingserver.health.service.HealthService;

public class HealthController {
  private final HealthService healthService;

  public HealthController(HealthService healthService) {
    this.healthService = healthService;
  }



}
