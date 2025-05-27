package org.cdpg.dx.auditingserver.report.helper;

import static org.cdpg.dx.auditingserver.report.util.Constants.EMPTY_FILE;
import static org.cdpg.dx.auditingserver.report.util.Constants.TOO_MANY_ROWS;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.report.model.ActivityLog;

public class CsvGenerator {
  private static final Logger LOGGER = LogManager.getLogger(CsvGenerator.class);

  public Future<String> toCsv(List<ActivityLog> rows) {
    try {
      if (rows == null || rows.isEmpty()) {
        return Future.succeededFuture(EMPTY_FILE);
      }

      if (rows.size() > 1000000) {
        return Future.succeededFuture(TOO_MANY_ROWS);
      }
      String filePath = "/home/ankit/Desktop/" + UUID.randomUUID() + ".csv";
      LOGGER.trace("file name with path :: {}", filePath);
      JsonObject firstRow = rows.get(0).toJson();
      Set<String> headers = firstRow.fieldNames();
      String headerLine = headers.stream().collect(Collectors.joining(","));

      StringBuilder sb = new StringBuilder();
      sb.append(headerLine).append("\n");

      for (ActivityLog log : rows) {
        JsonObject row = log.toJson();
        String line =
            headers.stream().map(h -> escapeCsv(row.getValue(h))).collect(Collectors.joining(","));
        sb.append(line).append("\n");
      }

      Files.write(Paths.get(filePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
      return Future.succeededFuture(filePath);
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  private String escapeCsv(Object value) {
    if (value == null) return "";
    String str = value.toString();
    if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
      str = str.replace("\"", "\"\"");
      return "\"" + str + "\"";
    }
    return str;
  }
}
