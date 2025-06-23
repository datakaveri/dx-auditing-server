package org.cdpg.dx.database.immudb.service;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import java.util.List;
import org.cdpg.dx.database.immudb.query.InsertQuery;

@VertxGen
@ProxyGen
public interface ImmudbService {

  Future<Boolean> executeQuery(InsertQuery insertQuery, String verificatioFild);

  static ImmudbService createProxy(Vertx vertx, String address) {
    return new ImmudbServiceVertxEBProxy(vertx, address);
  }
}