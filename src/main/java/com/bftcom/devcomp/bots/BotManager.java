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
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(BotManager.class);
  private static ObjectMapper mapper = new ObjectMapper();

  private static final ConcurrentHashMap<String, BotSession> botSessions = new ConcurrentHashMap<>();
  private static final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

  private ConnectionFactory factory;
  private Connection connection;
  private Channel channel;


  public BotManager() {
    factory = new ConnectionFactory();
    try {
      connection = factory.newConnection();
      Channel channel = connection.createChannel();
      AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QueuesConfiguration.MANAGEMENT_QUEUE, false, false, false, null);
      channel.addShutdownListener(new ShutdownListener() {
        @Override
        public void shutdownCompleted(ShutdownSignalException cause) {
          logger.info("shutting down channel " + declareOk.getQueue());
        }
      });

      String[] _consumerTag = new String[1];
      Consumer consumer = new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {

          String sMsg = new String(body, StandardCharsets.UTF_8);
          Message message = mapper.readValue(sMsg, Message.class);
          logger.info(" [x] Received '" + message + "'");
          
          if (message.getCommand().equals("cancel")) {
            channel.basicCancel(_consumerTag[0]);
          } else if (message.getCommand().equals(Commands.START_BOT.name())) {
            startBotSession(message.getProperties().get(Configuration.BOT_TOKEN), message.getProperties());
          } else if (message.getCommand().equals(Commands.STOP_BOT.name())) {
            stopBotSession(message.getProperties().get("botToken"));
          }
        } 
      };
      _consumerTag[0] = channel.basicConsume(QueuesConfiguration.MANAGEMENT_QUEUE, true, consumer);

    } catch (IOException | TimeoutException e) {
      logger.error("", e);
    }


  }

  @Override
  public boolean startBotSession(String id, Map<String, String> config) {
  logger.info("startBotSession " + id + " " + config.toString());
  //prevent starting bot sessions with the same id
  if (id != null) {
    synchronized (botSessions) {
      if (botSessions.get(id) != null) {
        logger.warn("prevent to start duplicate bot sessions with the same id " + id);
        return true;
      }
    }
  }
    BotOptions botOptions = new BotOptions();
    String proxyHost = config.get(Configuration.PROXY_HOST);
    String proxyPort = config.get(Configuration.PROXY_PORT);
    
    //set proxy options only if both proxyHost and poxyPort are defined
    if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && Integer.parseInt(proxyPort) > 0) {
      botOptions.setProxyHost(proxyHost);
      botOptions.setProxyPort(Integer.parseInt(proxyPort));
    }
    
    Bot bot = new Bot(botOptions) {
      @Override
      public String getBotUsername() {
        return config.get(Configuration.BOT_USERNAME);
      }

      @Override
      public String getBotToken() {
        return config.get(Configuration.BOT_TOKEN);
      }
    };
    
    try {
      logger.info("regestring bot " + id + " " + config.toString());
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

  public static void main(String[] args) {
    BotManager botManager = new BotManager();
  }
}
