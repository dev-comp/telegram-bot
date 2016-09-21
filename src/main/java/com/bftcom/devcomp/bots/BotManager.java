package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.IBotManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.BotOptions;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public class BotManager implements IBotManager {
  String managementQueueName = "ManagementQueue";
//  # ��� �������������� ������� ��� ��������� �� ����������� �����
  String botQueue = "BotQueue";
  public static final String TELEGRAM_ADAPTER = "telegram-adapter";
  


  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(BotManager.class);
  @SuppressWarnings("PackageAccessibility")
  private static ObjectMapper mapper = new ObjectMapper();

  private static final ConcurrentHashMap<String, BotSession> botSessions = new ConcurrentHashMap<>();
  private static final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

  private ConnectionFactory factory;
  private Connection connection;
  private Channel channel;


  public BotManager() {
    logger.info("creating a new bot manager adapter");
    factory = new ConnectionFactory();
    try {
      connection = factory.newConnection();
      Channel channel = connection.createChannel();
//      String adapterQueueName = IBotConst.QUEUE_SERVICE_PREFIX + managementQueueName;
      String adapterQueueName = IBotConst.QUEUE_ADAPTER_PREFIX + TELEGRAM_ADAPTER;
      AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(adapterQueueName, false, false, false, null);
      channel.addShutdownListener(cause -> logger.info("shutting down channel " + declareOk.getQueue()));

      String[] _consumerTag = new String[1];
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
          logger.error("consumerTag=" + consumerTag);
          logger.warn("consumerTag=" + consumerTag);
          logger.debug("consumerTag=" + consumerTag);
          logger.info("consumerTag=" + consumerTag);

          String sMsg = new String(body, StandardCharsets.UTF_8);
          Message message = mapper.readValue(sMsg, Message.class);
          logger.info(" [xxx] Received '" + message + "'");

          if (message.getCommand().equals(BotCommand.ADAPTER_STOP_BOT)) {
            logger.info("lets try to stop one bot session");
            stopBotSession(message.getUserProperties().get(Configuration.BOT_TOKEN));
            //channel.basicCancel(_consumerTag[0]);
//          } else if (message.getCommand().equals(BotCommands.START_BOT.name())) {
//            startBotSession(message.getServiceProperties().get(Configuration.BOT_TOKEN), message.getServiceProperties());
//          } else if (message.getCommand().equals(BotCommands.STOP_BOT.name())) {
//            stopBotSession(message.getServiceProperties().get(Configuration.BOT_TOKEN));
//          }
          } else if (message.getCommand().equals(BotCommand.ADAPTER_STOP_ALL_BOTS)) {
            //channel.basicCancel(_consumerTag[0]);
            logger.info("lets try to stop all bot sessions");
          } else if (message.getCommand().equals(BotCommand.ADAPTER_START_BOT)) {
            logger.info("lets try to start bot session");
            startBotSession(message.getUserProperties().get(Configuration.BOT_TOKEN),
                    message.getUserProperties(), message.getServiceProperties());
          }
        }
      };
      logger.debug("subscribing to a queue " + adapterQueueName);
      _consumerTag[0] = channel.basicConsume(adapterQueueName, true, consumer);

    } catch (IOException | TimeoutException e) {
      logger.error("", e);
    }
  }

  @Override
  public boolean startBotSession(String id, Map<String, String> userProps, Map<String, String> serviceProp) {
    logger.info("startBotSession id=" + id + ";config=" + userProps.toString());
    //prevent starting bot sessions with the same id
    if (id != null) {
      synchronized (botSessions) {
        if (botSessions.get(id) != null) {
          logger.warn("prevent to start duplicate bot sessions with the same id " + id);
          return true;
        }
      }
    }
    BotOptions botOptions = getBotOptionsWithProxyConfig(userProps);
    Bot bot = new Bot(botOptions) {
      @Override
      public String getBotUsername() {
        logger.debug("getBotUserName=" + userProps.get(Configuration.BOT_USERNAME));
        return userProps.get(Configuration.BOT_USERNAME);
      }

      @Override
      public String getBotToken() {
        logger.debug("getBotToken ");
        logger.debug(userProps.get(Configuration.BOT_TOKEN));
        return userProps.get(Configuration.BOT_TOKEN);
      }
    };

    try {
      logger.warn("creating queues");
      bot.setName(serviceProp.get(IBotConst.PROP_BOT_NAME));

      String outQueueName = IBotConst.QUEUE_BOT_PREFIX + bot.getName();
      Channel outChannel = createChannel(outQueueName);

      String inQueueName = IBotConst.QUEUE_SERVICE_PREFIX + "BotQueue";
      Channel inChannel = createChannel(inQueueName);

      bot.setInQueueName(inQueueName);
      bot.setOutQueueName(outQueueName);
      bot.setInChannel(inChannel);
      bot.setOutChannel(outChannel);
    } catch (IOException e) {
      return false;
    }

    try {
      logger.info("registering bot " + id + " " + userProps.toString());
      BotSession botSession = telegramBotsApi.registerBot(bot);
      synchronized (botSessions) {
        botSessions.put(id, botSession);
      }
    } catch (TelegramApiException e) {
      logger.warn("", e);
      return false;
    }
    return true;
  }

  private BotOptions getBotOptionsWithProxyConfig(Map<String, String> config) {
    BotOptions botOptions = new BotOptions();
    String proxyHost = config.get(Configuration.PROXY_HOST);
    String proxyPort = config.get(Configuration.PROXY_PORT);

    //set proxy options only if both proxyHost and poxyPort are defined
    if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && Integer.parseInt(proxyPort) > 0) {
      botOptions.setProxyHost(proxyHost);
      botOptions.setProxyPort(Integer.parseInt(proxyPort));
    }
    return botOptions;
  }

  @Override
  public boolean stopBotSession(String id) {
    synchronized (botSessions) {
      BotSession botSession = botSessions.remove(id);
      if (botSession != null) {
        logger.info("bot session for bot " + id + " is closed.");
        botSession.close();
      }
    }
    return true;
  }

  @Override
  public void stopAllBotSessions() {
    logger.debug("stopping all bot sessions");
    synchronized (botSessions) {
      for (String botSessionKey : botSessions.keySet()) {
        BotSession botSession = botSessions.remove(botSessionKey);
        if (botSession != null) {
          botSession.close();
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (connection != null) {
      connection.close();
    }
    super.finalize();
  }

  private Channel createChannel(String queueName) throws IOException {
    Channel channel = null;
    try {
      channel = connection.createChannel();
      AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(queueName, false, false, false, null);
      channel.addShutdownListener(cause -> logger.info("shutting down channel " + declareOk.getQueue()));
    } catch (IOException e) {
      logger.error("", e);
      throw e;
    }
    return channel;
  }

  public static void main(String[] args) {
    BotManager botManager = new BotManager();
  }
}
