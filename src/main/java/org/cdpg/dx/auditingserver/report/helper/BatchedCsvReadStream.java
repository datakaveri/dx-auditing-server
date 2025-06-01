package org.cdpg.dx.auditingserver.report.helper;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import org.cdpg.dx.auditingserver.report.dao.ActivityLogDao;

public class BatchedCsvReadStream implements ReadStream<Buffer> {
  private final ActivityLogDao dao;
  private final CsvGenerator generator;
  private final Vertx vertx;
  private final int limit;
  private final long totalCount;
  private final Queue<Buffer> bufferQueue = new LinkedList<>();
  private final String userId;
  private int offset;
  private boolean writeHeader = true;
  private Handler<Buffer> dataHandler;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;
  private boolean paused = false;
  private boolean ended = false;

  public BatchedCsvReadStream(
      ActivityLogDao dao,
      CsvGenerator generator,
      Vertx vertx,
      int limit,
      long totalCount,
      int offset,
      UUID userId) {
    this.dao = dao;
    this.generator = generator;
    this.vertx = vertx;
    this.limit = limit;
    this.totalCount = totalCount;
    this.offset = offset;
    if (userId == null) {
      this.userId = null;
    } else {
      this.userId = userId.toString();
    }
    fetchNextBatch();
  }

  private void fetchNextBatch() {
    if (offset >= totalCount) {
      ended = true;
      if (endHandler != null) endHandler.handle(null);
      return;
    }

    dao.getCsvGeneratedByPaginated(limit, offset, userId)
        .compose(
            batch -> {
              if (batch.isEmpty()) {
                offset += limit;
                fetchNextBatch();
                return Future.failedFuture("empty");
              }
              return generator.toCsvStream(batch, vertx, writeHeader);
            })
        .onSuccess(
            csvStream -> {
              writeHeader = false;
              csvStream.handler(
                  buffer -> {
                    if (paused) {
                      bufferQueue.add(buffer);
                    } else if (dataHandler != null) {
                      dataHandler.handle(buffer);
                    }
                  });
              csvStream.endHandler(
                  v -> {
                    offset += limit;
                    if (!paused) {
                      fetchNextBatch();
                    }
                  });
              csvStream.exceptionHandler(
                  e -> {
                    if (exceptionHandler != null) exceptionHandler.handle(e);
                  });
            })
        .onFailure(
            e -> {
              if (!"empty".equals(e.getMessage()) && exceptionHandler != null) {
                exceptionHandler.handle(e);
              }
            });
  }

  @Override
  public ReadStream<Buffer> handler(Handler<Buffer> handler) {
    this.dataHandler = handler;
    vertx.runOnContext(
        v -> {
          while (!paused && !bufferQueue.isEmpty() && dataHandler != null) {
            dataHandler.handle(bufferQueue.poll());
          }
        });
    return this;
  }

  @Override
  public ReadStream<Buffer> pause() {
    paused = true;
    return this;
  }

  @Override
  public ReadStream<Buffer> resume() {
    if (paused) {
      paused = false;
      vertx.runOnContext(
          v -> {
            while (!paused && !bufferQueue.isEmpty() && dataHandler != null) {
              dataHandler.handle(bufferQueue.poll());
            }
            if (!paused && !ended && offset < totalCount) {
              fetchNextBatch();
            }
          });
    }
    return this;
  }

  @Override
  public ReadStream<Buffer> fetch(long amount) {
    return this;
  }

  @Override
  public ReadStream<Buffer> endHandler(Handler<Void> handler) {
    this.endHandler = handler;
    return this;
  }

  @Override
  public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }
}
