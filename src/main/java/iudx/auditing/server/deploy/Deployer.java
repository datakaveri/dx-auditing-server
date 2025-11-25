package iudx.auditing.server.deploy;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.zookeeper.ZookeeperDiscoveryProperties;
import com.hazelcast.zookeeper.ZookeeperDiscoveryStrategyFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.cli.TypedOption;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/** The Deployer class is responsible for deploying applications to a target environment. */
public class Deployer {
  private static final Logger LOGGER = LogManager.getLogger(Deployer.class);
  private static ClusterManager mgr;
  private static Vertx vertx;

  /**
   * Deploys clustered vert.x instance of the server. As a JAR, the application requires 3 runtime
   * arguments:
   *
   * <ul>
   *   <li>--config/-c : path to the config file
   *   <li>--hostname/-i : the hostname for clustering
   *   <li>--modules/-m : comma separated list of module names to deploy
   * </ul>
   *
   * e.g. <i>java -jar ./fatjar.jar --host $(hostname) -c configs/config.json -m</i>
   */
  public static void recursiveDeploy(Vertx vertx, JsonObject configs, int i) {
    if (i >= configs.getJsonArray("modules").size()) {
      LOGGER.info("Deployed all");
      return;
    }
    JsonObject config = configs.getJsonArray("modules").getJsonObject(i);
    String moduleName = config.getString("id");
    int numInstances = config.getInteger("verticleInstances");
    vertx.deployVerticle(
        moduleName,
        new DeploymentOptions().setInstances(numInstances).setConfig(config),
        ar -> {
          if (ar.succeeded()) {
            LOGGER.info("Deployed {}", moduleName);
            recursiveDeploy(vertx, configs, i + 1);
          } else {
            LOGGER.fatal("Failed to deploy {} cause:", moduleName, ar.cause());
          }
        });
  }

  /**
   * Recursively deploy modules/verticles (if they exist) present in the `modules` list.
   *
   * @param vertx the vert.x instance
   * @param configs the JSON configuration
   * @param modules the list of modules to deploy
   */
  public static void recursiveDeploy(Vertx vertx, JsonObject configs, List<String> modules) {
    if (modules.isEmpty()) {
      LOGGER.info("Deployed requested verticle(s)");
      return;
    }

    JsonArray configuredModules = configs.getJsonArray("modules");

    String moduleName = modules.get(0);
    JsonObject config =
        configuredModules.stream()
            .map(obj -> (JsonObject) obj)
            .filter(obj -> obj.getString("id").equals(moduleName))
            .findFirst()
            .orElse(new JsonObject());

    if (config.isEmpty()) {
      LOGGER.fatal("Failed to deploy {} cause: Not Found", moduleName);
      return;
    }

    int numInstances = config.getInteger("verticleInstances");
    vertx.deployVerticle(
        moduleName,
        new DeploymentOptions().setInstances(numInstances).setConfig(config),
        ar -> {
          if (ar.succeeded()) {
            LOGGER.info("Deployed {}", moduleName);
            modules.remove(0);
            recursiveDeploy(vertx, configs, modules);
          } else {
            LOGGER.fatal("Failed to deploy {} cause:", moduleName, ar.cause());
          }
        });
  }

  public static ClusterManager getClusterManager(
      String host, List<String> zookeepers, String clusterId) {
    Config config = new Config();
    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
    config.getNetworkConfig().setPublicAddress(host);
    config.setProperty("hazelcast.discovery.enabled", "true");
    config.setProperty("hazelcast.logging.type", "log4j2");
    DiscoveryStrategyConfig discoveryStrategyConfig =
        new DiscoveryStrategyConfig(new ZookeeperDiscoveryStrategyFactory());
    discoveryStrategyConfig.addProperty(
        ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), String.join(",", zookeepers));
    discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.GROUP.key(), clusterId);
    config
        .getNetworkConfig()
        .getJoin()
        .getDiscoveryConfig()
        .addDiscoveryStrategyConfig(discoveryStrategyConfig);

    return new HazelcastClusterManager(config);
  }

  public static MetricsOptions getMetricsOptions() {
    // Best-effort metrics configuration.
    return new MicrometerMetricsOptions()
        .setPrometheusOptions(
            new VertxPrometheusOptions()
                .setEnabled(true)
                .setStartEmbeddedServer(true)
                .setEmbeddedServerOptions(
                    new HttpServerOptions()
                        .setPort(9000)
                        // Important in Docker/Swarm
                        .setHost("0.0.0.0")))
        .setLabels(
            EnumSet.of(Label.EB_ADDRESS, Label.EB_FAILURE, Label.HTTP_CODE, Label.HTTP_METHOD))
        .setEnabled(true);
  }

  public static void setJvmMetrics() {
    MeterRegistry registry = BackendRegistries.getDefaultNow();
    if (registry == null) {
      LOGGER.warn("No default Micrometer registry found; JVM metrics will not be bound.");
      return;
    }
    new JvmMemoryMetrics().bindTo(registry);
    new JvmGcMetrics().bindTo(registry);
    new ProcessorMetrics().bindTo(registry);
    new JvmThreadMetrics().bindTo(registry);
  }

  /**
   * Deploy clustered vert.x instance.
   *
   * @param configPath the path for JSON config file
   * @param host host
   * @param modules list of modules to deploy. If list is empty, all modules are deployed
   */
  public static void deploy(String configPath, String host, List<String> modules) {
    String config;
    try {
      config = Files.readString(Paths.get(configPath));
    } catch (Exception e) {
      LOGGER.fatal("Couldn't read configuration file", e);
      return;
    }
    if (config.length() < 1) {
      LOGGER.fatal("Couldn't read configuration file: empty content");
      return;
    }
    JsonObject configuration = new JsonObject(config);
    List zookeepers = configuration.getJsonArray("zookeepers").getList();
    String clusterId = configuration.getString("clusterId");
    mgr = getClusterManager(host, zookeepers, clusterId);
    EventBusOptions ebOptions = new EventBusOptions().setClusterPublicHost(host);

    VertxOptions options = new VertxOptions().setClusterManager(mgr).setEventBusOptions(ebOptions);

    // Make metrics "best-effort": if Micrometer/Prometheus is not available, continue without.
    try {
      options.setMetricsOptions(getMetricsOptions());
    } catch (Throwable t) {
      LOGGER.error(
          "Failed to initialize Micrometer/Prometheus metrics. Continuing without metrics.", t);
    }

    Vertx.clusteredVertx(
        options,
        res -> {
          LOGGER.info("Clustered vert.x instance result received {}", res.succeeded());
          if (res.succeeded()) {
            vertx = res.result();
            LOGGER.debug("Metrics enabled: {}", vertx.isMetricsEnabled());
            try {
              setJvmMetrics();
            } catch (Throwable t) {
              LOGGER.warn("Failed to bind JVM metrics", t);
            }

            if (modules.isEmpty()) {
              recursiveDeploy(vertx, configuration, 0);
            } else {
              recursiveDeploy(vertx, configuration, modules);
            }

          } else {
            LOGGER.fatal("Could not join cluster :{}", res.cause().getMessage(), res.cause());
          }
        });
  }

  public static void gracefulShutdown() {
    Logger logger = LogManager.getLogger(Deployer.class);

    if (vertx == null) {
      logger.error(
          "Vertx instance is null – cluster was never initialized or failed to start. "
              + "Skipping graceful shutdown.");
      // Still try to shutdown log4j cleanly
      if (LogManager.getContext() instanceof LoggerContext) {
        logger.debug("Shutting down log4j2");
        LogManager.shutdown(LogManager.getContext());
      } else {
        logger.warn("Unable to shutdown log4j2");
      }
      return;
    }

    Set<String> deployIdSet = vertx.deploymentIDs();
    logger.info("Shutting down the application");
    CountDownLatch latchVerticles = new CountDownLatch(deployIdSet.size());
    CountDownLatch latchCluster = new CountDownLatch(1);
    CountDownLatch latchVertx = new CountDownLatch(1);

    logger.debug("Number of verticles being undeployed: {}", deployIdSet.size());
    for (String deploymentId : deployIdSet) {
      vertx.undeploy(
          deploymentId,
          handler -> {
            if (handler.succeeded()) {
              logger.debug("{} verticle successfully undeployed", deploymentId);
              latchVerticles.countDown();
            } else {
              logger.warn("{} undeploy failed!", deploymentId, handler.cause());
              latchVerticles.countDown();
            }
          });
    }

    try {
      latchVerticles.await(5, TimeUnit.SECONDS);
      logger.info("All the verticles undeployed (or timeout reached)");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Interrupted while waiting for verticles to undeploy", e);
    }

    // Leave the cluster if ClusterManager is available
    if (mgr != null) {
      Promise<Void> promise = Promise.promise();
      mgr.leave(promise);
      promise
          .future()
          .onComplete(
              ar -> {
                if (ar.succeeded()) {
                  logger.info("Vert.x left cluster successfully");
                } else {
                  logger.warn("Error while leaving cluster", ar.cause());
                }
                latchCluster.countDown();
              });
    } else {
      logger.warn("ClusterManager is null; skipping cluster leave()");
      latchCluster.countDown();
    }

    try {
      latchCluster.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Interrupted while waiting for cluster leave", e);
    }

    // Close Vert.x instance
    vertx.close(
        handler -> {
          if (handler.succeeded()) {
            logger.info("Vert.x closed successfully");
          } else {
            logger.warn("Vert.x didn't close properly, reason:", handler.cause());
          }
          latchVertx.countDown();
        });

    try {
      latchVertx.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.warn("Interrupted while waiting for Vert.x to close", e);
    }

    // then shut down log4j
    if (LogManager.getContext() instanceof LoggerContext) {
      logger.debug("Shutting down log4j2");
      LogManager.shutdown(LogManager.getContext());
    } else {
      logger.warn("Unable to shutdown log4j2");
    }
  }

  public static void main(String[] args) {
    CLI cli =
        CLI.create("IUDX Auditing Server")
            .setSummary("A CLI to deploy the auditing server")
            .addOption(
                new Option()
                    .setLongName("help")
                    .setShortName("h")
                    .setFlag(true)
                    .setDescription("display help"))
            .addOption(
                new Option()
                    .setLongName("config")
                    .setShortName("c")
                    .setRequired(true)
                    .setDescription("configuration file"))
            .addOption(
                new Option()
                    .setLongName("host")
                    .setShortName("i")
                    .setRequired(true)
                    .setDescription("public host"))
            .addOption(
                new TypedOption<String>()
                    .setType(String.class)
                    .setLongName("modules")
                    .setShortName("m")
                    .setRequired(false)
                    .setDefaultValue("all")
                    .setParsedAsList(true)
                    .setDescription(
                        "comma separated list of verticle names to deploy. "
                            + "If omitted, or if `all` is passed, all verticles are deployed"));
    StringBuilder usageString = new StringBuilder();
    cli.usage(usageString);
    CommandLine commandLine = cli.parse(Arrays.asList(args), false);
    if (commandLine.isValid() && !commandLine.isFlagEnabled("help")) {
      String configPath = commandLine.getOptionValue("config");
      String host = commandLine.getOptionValue("host");
      List<String> passedModules = commandLine.getOptionValues("modules");
      List<String> modules = passedModules.stream().distinct().collect(Collectors.toList());

      /* `all` is also passed by default if no -m option given.*/
      if (modules.contains("all")) {
        deploy(configPath, host, List.of());
      } else {
        deploy(configPath, host, modules);
      }

      Runtime.getRuntime().addShutdownHook(new Thread(Deployer::gracefulShutdown));
    } else {
      LOGGER.info(usageString);
    }
  }
}
