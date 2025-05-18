package org.cdpg.dx.auth.authentication.handler;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.auth.authentication.client.KeyCloakSecretKeyClientImpl;
import org.cdpg.dx.auth.authentication.exception.AuthenticationException;
import org.cdpg.dx.auth.authentication.service.JwtAuthProviderForKc;
import org.cdpg.dx.auth.authentication.service.JwtAuthenticatorServiceImpl;
import org.cdpg.dx.util.RoutingContextHelper;

public class KeycloakAuthenticationHandler implements AuthenticationHandler {
  private static final Logger LOGGER = LogManager.getLogger(KeycloakAuthenticationHandler.class);
  private final JsonObject config;
  private final Vertx vertx;
  private final KeyCloakSecretKeyClientImpl keyCloakSecretKeyClient;
  private JWTAuth jwtAuth;

  public KeycloakAuthenticationHandler(JsonObject config, Vertx vertx) {
    this.config = config;
    this.vertx = vertx;
    this.keyCloakSecretKeyClient = new KeyCloakSecretKeyClientImpl(config, vertx);

    JwtAuthProviderForKc.init(vertx, config, keyCloakSecretKeyClient)
        .onSuccess(
            jwtAuth -> {
              this.jwtAuth = jwtAuth;
              LOGGER.info("JWT Auth initialized successfully for Keycloak");
            })
        .onFailure(
            err -> {
              LOGGER.error("Failed to initialize JWT Auth: {}", err.getMessage());
            });
  }

  @Override
  public void handle(RoutingContext context) {
    LOGGER.info("TokenAuthenticationHandler: handle method called");
    String token = context.request().getHeader("token");

    JwtAuthenticatorServiceImpl jwtAuthenticator = new JwtAuthenticatorServiceImpl(jwtAuth);
    jwtAuthenticator
        .authenticate(token)
        .onSuccess(
            jwtData -> {
              LOGGER.info("Token decoded successfully: {}", jwtData);
              RoutingContextHelper.setJwtData(context, jwtData);
              context.next();
            })
        .onFailure(
            err -> {
              context.fail(401, new AuthenticationException(err.getMessage()));
            });
  }
}
