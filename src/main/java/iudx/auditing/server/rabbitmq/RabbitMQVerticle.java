package iudx.auditing.server.rabbitmq;

import static iudx.auditing.server.common.Constants.MSG_PROCESS_ADDRESS;
import static iudx.auditing.server.common.Constants.RMQ_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.auditing.server.common.IConsumer;
import iudx.auditing.server.common.VHosts;
import iudx.auditing.server.processor.MessageProcessService;
import iudx.auditing.server.rabbitmq.consumers.AuditMessageConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMQVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(RabbitMQVerticle.class);

  private RabbitMQService rabbitMQService;

  private RabbitMQOptions config;
  private RabbitMQClient client;
  private String dataBrokerIP;
  private int dataBrokerPort;
  private int dataBrokerManagementPort;
  private String dataBrokerUserName;
  private String dataBrokerPassword;
  private int connectionTimeout;
  private int requestedHeartbeat;
  private int handshakeTimeout;
  private int requestedChannelMax;
  private int networkRecoveryInterval;
  private IConsumer auditConsumer;
  private MessageProcessService messageProcessService;

  private WebClientOptions webConfig;

  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;

  @Override
  public void start() throws Exception {

    dataBrokerIP = config().getString("dataBrokerIP");
    dataBrokerPort = config().getInteger("dataBrokerPort");
    dataBrokerManagementPort = config().getInteger("dataBrokerManagementPort");
    dataBrokerUserName = config().getString("dataBrokerUserName");
    dataBrokerPassword = config().getString("dataBrokerPassword");
    connectionTimeout = config().getInteger("connectionTimeout");
    requestedHeartbeat = config().getInteger("requestedHeartbeat");
    handshakeTimeout = config().getInteger("handshakeTimeout");
    requestedChannelMax = config().getInteger("requestedChannelMax");
    networkRecoveryInterval = config().getInteger("networkRecoveryInterval");

    /* Configure the RabbitMQ Data Broker client with input from config files. */

    config = new RabbitMQOptions();
    config.setUser(dataBrokerUserName);
    config.setPassword(dataBrokerPassword);
    config.setHost(dataBrokerIP);
    config.setPort(dataBrokerPort);
    config.setConnectionTimeout(connectionTimeout);
    config.setRequestedHeartbeat(requestedHeartbeat);
    config.setHandshakeTimeout(handshakeTimeout);
    config.setRequestedChannelMax(requestedChannelMax);
    config.setNetworkRecoveryInterval(networkRecoveryInterval);
    config.setAutomaticRecoveryEnabled(true);
    webConfig = new WebClientOptions();
    webConfig.setKeepAlive(true);
    webConfig.setConnectTimeout(86400000);
    webConfig.setDefaultHost(dataBrokerIP);
    webConfig.setDefaultPort(dataBrokerManagementPort);
    webConfig.setKeepAliveTimeout(86400000);

    RabbitMQOptions internalVhostOptions = new RabbitMQOptions(config);
    String internalCommVhost = config().getString(VHosts.IUDX_INTERNAL.value);
    internalVhostOptions.setVirtualHost(internalCommVhost);
    rabbitMQService = new RabbitMQServiceImpl(vertx, internalVhostOptions);

    RabbitMQOptions prodOptions = new RabbitMQOptions(config);
    String prodVhost = config().getString(VHosts.IUDX_PROD.value);
    prodOptions.setVirtualHost(prodVhost);
    messageProcessService = MessageProcessService.createProxy(vertx, MSG_PROCESS_ADDRESS);

    auditConsumer = new AuditMessageConsumer(vertx, internalVhostOptions, messageProcessService);
    auditConsumer.start();
    binder = new ServiceBinder(vertx);

    consumer =
        binder.setAddress(RMQ_SERVICE_ADDRESS).register(RabbitMQService.class, rabbitMQService);
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
