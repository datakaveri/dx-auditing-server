package iudx.auditing.server.processor.subscription.catService;

import static iudx.auditing.server.common.Constants.CAT_SEARCH_PATH;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CatalogueServiceImpl implements CatalogueService {
  private static final Logger LOGGER = LogManager.getLogger(CatalogueServiceImpl.class);

  private final int port;
  private final String host;

  private WebClient webClient;
  private String catBasePath;
  private String catSearchPath;

  public CatalogueServiceImpl(Vertx vertx, JsonObject config) {
    this.port = config.getInteger("cataloguePort");
    this.host = config.getString("catalogueHost");
    this.catBasePath = config.getString("dxCatalogueBasePath");
    this.catSearchPath = catBasePath + CAT_SEARCH_PATH;

    WebClientOptions options = new WebClientOptions();
    options.setTrustAll(true).setVerifyHost(false).setSsl(true);
    webClient = WebClient.create(vertx, options);
  }

  /**
   * @param id
   * @return
   */
  @Override
  public Future<JsonObject> searchCatItem(String id) {
    LOGGER.debug("get item for id: {} ", id);
    Promise<JsonObject> promise = Promise.promise();

    webClient
        .get(port, host, catSearchPath)
        .addQueryParam("property", "[id]")
        .addQueryParam("value", "[[" + id + "]]")
        .addQueryParam("filter", "[id,provider]")
        .expect(ResponsePredicate.JSON)
        .send(
            relHandler -> {
              if (relHandler.succeeded()
                  && relHandler.result().bodyAsJsonObject().getInteger("totalHits") > 0) {
                JsonArray resultArray =
                    relHandler.result().bodyAsJsonObject().getJsonArray("results");
                JsonObject response = resultArray.getJsonObject(0);
                promise.complete(response);
              } else {
                LOGGER.error("catalogue call search api failed: " + relHandler.cause());
                promise.fail("catalogue call search api failed");
              }
            });

    return promise.future();
  }

}
