package org.cdpg.dx.auth.authentication.client;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyCloakSecretKeyClientImpl implements SecretKeyClient {
  private static final Logger LOGGER = LogManager.getLogger(KeyCloakSecretKeyClientImpl.class);

  private final WebClient webClient;
  private final String url;

  public KeyCloakSecretKeyClientImpl(JsonObject config, Vertx vertx) {
    this.url = config.getString("keycloakCertUrl");
    this.webClient = getWebClient(vertx);
  }

  private WebClient getWebClient(Vertx vertx) {
    WebClientOptions webClientOptions = new WebClientOptions();
    webClientOptions.setTrustAll(true).setVerifyHost(false).setSsl(url.startsWith("https"));
    return WebClient.create(vertx, webClientOptions);
  }

  @Override
  public Future<JsonObject> fetchCertKey() {
    LOGGER.info("Fetching certificate key from {}", url);
    return webClient
        .requestAbs(HttpMethod.GET, url)
        .send()
        .compose(
            response -> {
              if (response.statusCode() == 200) {
                JsonObject json = response.bodyAsJsonObject();
                if (json != null && json.containsKey("keys")) {
                  LOGGER.info("Successfully fetched certificate key.");
                  return Future.succeededFuture(json);
                } else {
                  LOGGER.error("Response does not contain 'keys' field.");
                  return Future.failedFuture("Response does not contain 'keys' field.");
                }
              } else {
                String errorMessage =
                    "Failed to fetch JWT public key, HTTP status: " + response.statusCode();
                LOGGER.error(errorMessage);
                return Future.failedFuture(errorMessage);
              }
            })
        .recover(
            error -> {
              LOGGER.error("Error fetching certificate key: {}", error.getMessage());
              return Future.failedFuture(error);
            });
  }
}
