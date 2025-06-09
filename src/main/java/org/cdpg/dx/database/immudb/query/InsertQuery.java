package org.cdpg.dx.database.immudb.query;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auditingserver.activity.service.impl.ActivityServiceImpl;
import org.cdpg.dx.common.exception.DxImmudbQueryException;
import org.cdpg.dx.database.postgres.models.Query;

@JsonGen
@DataObject
public class InsertQuery implements Query {
  private static final Logger LOGGER = LogManager.getLogger(InsertQuery.class);
  private String table;
  private List<String> columns;
  private List<Object> values;

  public InsertQuery() {
  }

  public InsertQuery(String table, List<String> columns, List<Object> values) {
    this.table = table;
    this.columns = columns;
    this.values = values;
  }

  public InsertQuery(InsertQuery other) {
    this.table = other.getTable();
    this.columns = other.getColumns();
    this.values = other.getValues();
  }

  public InsertQuery(JsonObject json) {
    InsertQueryConverter.fromJson(json, this);
  }

  public String getTable() {
    return table;
  }

  public InsertQuery setTable(String table) {
    this.table = table;
    return this;
  }

  public List<String> getColumns() {
    return columns;
  }

  public InsertQuery setColumns(List<String> columns) {
    LOGGER.debug("Setting columns for InsertQuery: {}", columns);
    this.columns = columns;
    return this;
  }

  public List<Object> getValues() {
    return values;
  }

  public InsertQuery setValues(List<Object> values) {
    LOGGER.debug("Setting values for InsertQuery: {}", values);
    this.values = values;
    return this;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    InsertQueryConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toSQL() {
    if (columns == null || columns.isEmpty()) {
      throw new DxImmudbQueryException("Columns cannot be null or empty for InsertQuery");
    }
    if (values == null || values.size() != columns.size()) {
      throw new DxImmudbQueryException("Values must not be null and must match columns size");
    }
    String valueLiterals = values.stream()
        .map(v -> v == null ? "NULL" : (
            v instanceof String ? "'" + v.toString().replace("'", "''") + "'" : v.toString()))
        .collect(Collectors.joining(", "));
    return "INSERT INTO " + table +
        " (" + String.join(", ", columns) + ") " +
        "VALUES (" + valueLiterals + ")";
  }

  @Override
  public List<Object> getQueryParams() {
    return values;
  }

  @Override
  public String toString() {
    return "InsertQuery{" +
        "table='" + table + '\'' +
        ", columns=" + columns +
        ", values=" + values +
        '}';
  }
}