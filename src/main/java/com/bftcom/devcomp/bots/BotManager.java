package com.bftcom.devcomp.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.BotOptions;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public class BotManager implements IBotManager {
  private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

  private static final ConcurrentHashMap<String, BotSession> botSessions = new ConcurrentHashMap<>();
  private static final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

  @Override
  public boolean startBotSession(String string, String proxyHost, Integer proxyPort) {
    BotOptions botOptions = new BotOptions();
    if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && proxyPort > 0) {
      botOptions.setProxyHost(proxyHost);
      botOptions.setProxyPort(proxyPort);
    }
    EchoBot bot = new EchoBot(botOptions);
    try {
      BotSession botSession = telegramBotsApi.registerBot(bot);
      synchronized (botSessions) {
        botSessions.put(bot.getBotToken(), botSession);
      }
    } catch (TelegramApiException e) {
      logger.warn("", e);
      return false;
    }
    return true;
  }

  @Override
  public boolean stopBotSession(String botToken) {
    synchronized (botSessions) {
      BotSession botSession = botSessions.remove(botToken);
      if (botSession != null) {
        logger.info("bot session for bot " + botToken + " is closed.");
        botSession.close();
      }
    }
    return true;
  }

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
}
