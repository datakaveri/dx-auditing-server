package org.cdpg.dx.auditingserver.report.service;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import java.util.UUID;

public interface ReportService {

  Future<ReadStream<Buffer>> streamAdminCsvBatched();

  Future<ReadStream<Buffer>> streamConsumerCsvBatched(UUID userId);
}
