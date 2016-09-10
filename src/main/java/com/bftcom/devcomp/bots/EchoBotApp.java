package com.bftcom.devcomp.bots;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.updatesreceivers.BotSession;


public class EchoBotApp {
  private static final Logger logger = LoggerFactory.getLogger(EchoBotApp.class);

  public static void main(String[] args) {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();// assume SLF4J is bound to logback in the current environment
    StatusPrinter.print(lc);// print logback's internal status

    BotSession botSession = null;
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
    try {
      EchoBot bot = new EchoBot();
      botSession = telegramBotsApi.registerBot(bot);
    } catch (TelegramApiException e) {
      BotLogger.error("ERROR:", e);
    }

    final BotSession finalBotSession = botSession;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if (finalBotSession != null) {
          logger.info(EchoBotApp.class.getSimpleName() + " is shut down");
          finalBotSession.close();
        }
      }
    });
  }
}
