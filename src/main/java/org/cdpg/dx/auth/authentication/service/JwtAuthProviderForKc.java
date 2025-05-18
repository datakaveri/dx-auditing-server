package org.cdpg.dx.auth.authentication.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.cdpg.dx.auth.authentication.client.KeyCloakSecretKeyClientImpl;

public class JwtAuthProviderForKc {

  private static JWTAuth jwtAuthInstance;

  private JwtAuthProviderForKc() {
    // private constructor to prevent instantiation
  }

  public static Future<JWTAuth> init(
      Vertx vertx, JsonObject config, KeyCloakSecretKeyClientImpl keyCloakSecretKeyClient) {
    if (jwtAuthInstance != null) {
      return Future.succeededFuture(jwtAuthInstance);
    }

    return keyCloakSecretKeyClient
        .fetchCertKey()
        .compose(
            certJson -> {
              if (certJson == null || certJson.isEmpty()) {
                return Future.failedFuture("Public key (certificate) is empty or null");
              }
              List<JsonObject> jwks =
                  certJson.getJsonArray("keys").stream()
                      .map(obj -> (JsonObject) obj)
                      .collect(Collectors.toList());

              JWTAuthOptions options =
                  new JWTAuthOptions()
                      .setJwks(jwks)
                      .setJWTOptions(
                          new JWTOptions()
                              .setLeeway(30)
                              .setIgnoreExpiration(config.getBoolean("jwtIgnoreExpiry", false))
                              .setIssuer(config.getString("iss"))
                              .setAudience(Collections.singletonList(config.getString("aud"))));
              jwtAuthInstance = JWTAuth.create(vertx, options);
              return Future.succeededFuture(jwtAuthInstance);
            });
  }

  public static JWTAuth getInstance() {
    if (jwtAuthInstance == null) {
      throw new IllegalStateException("JWTAuthProvider not initialized. Call init() first.");
    }
    return jwtAuthInstance;
  }
}
