package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.Configuration;
import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.api.IBotManager;
import com.bftcom.devcomp.bots.queues.AdapterQueueConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.BotOptions;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public class BotManager implements IBotManager {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

  /**
   * Имя прослушиваемой очереди для сообщений от экземпляров ботов
   */
  private static final String TELEGRAM_ADAPTER = "telegram-adapter";

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
      channel = connection.createChannel();
      new AdapterQueueConsumer(channel, this, IBotConst.QUEUE_TO_ADAPTER_PREFIX + TELEGRAM_ADAPTER);
    } catch (IOException | TimeoutException e) {
      logger.error("", e);
    }
  }

  @Override
  public boolean startBotSession(String id, Map<String, String> userProps, Map<String, String> serviceProps) {
    logger.info("startBotSession id=" + id + ";userProperties=" + userProps.toString());
    //prevent starting bot sessions with the same id
    if (id != null) {
      synchronized (botSessions) {
        if (botSessions.get(id) != null) {
          logger.warn("prevented starting a duplicate bot session with the same id " + id);
          return true;
        }
      }
    }

    BotOptions botOptions = getBotOptionsWithProxyConfig(userProps);
    Bot bot = new Bot(botOptions, serviceProps.get(IBotConst.PROP_BOT_NAME), userProps.get(Configuration.BOT_USERNAME), userProps.get(Configuration.BOT_TOKEN));

    try {
      String outQueueName = IBotConst.QUEUE_TO_BOT_PREFIX + bot.getName();
      String inQueueName = IBotConst.QUEUE_FROM_BOT_PREFIX + bot.getName();

      logger.debug("creating queues");
      bot.setInQueueName(inQueueName);
      bot.setOutQueueName(outQueueName);
      bot.setInChannel(createChannel(inQueueName));
      bot.setOutChannel(createChannel(outQueueName));

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
