package com.bftcom.devcomp.adapters;

import com.bftcom.devcomp.api.AbstractMessengerAdapter;
import com.bftcom.devcomp.api.Configuration;
import com.bftcom.devcomp.api.IBot;
import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.bots.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.BotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public class TelegramAdapter extends AbstractMessengerAdapter<BotSession> {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(TelegramAdapter.class);

  /**
   * Имя прослушиваемой очереди для сообщений от экземпляров ботов
   */
  private static final String ADAPTER_NAME = "telegram-adapter";

  private final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();


  @Override
  protected boolean startBotSession(String id, IBot bot) {
    try {
      logger.info("registering bot " + id);
      BotSession botSession = telegramBotsApi.registerBot((TelegramLongPollingBot) bot);
      synchronized (botSessions) {
        botSessions.put(id, botSession);
      }
    } catch (TelegramApiException e) {
      logger.error("", e);
    }
    return true;
  }

  @Override
  protected IBot createNewBot(Map<String, String> serviceProps, Map<String, String> userProps) {
    BotOptions botOptions = getBotOptionsWithProxyConfig(userProps);
    return new TelegramBot(botOptions, serviceProps.get(IBotConst.PROP_BOT_NAME), userProps.get(Configuration.BOT_USERNAME), userProps.get(Configuration.BOT_TOKEN));
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
  public boolean stopBotSession(BotSession botSession) {
    synchronized (botSessions) {
      if (botSession != null) {
        botSession.close();
      }
    }
    return true;
  }


  @Override
  public String getAdapterName() {
    return ADAPTER_NAME;
  }

  public static void main(String[] args) {
    TelegramAdapter botManager = new TelegramAdapter();
    HashMap<String, String> serviceProps = new HashMap<>();
    serviceProps.put(IBotConst.PROP_BOT_NAME, "tester");
    HashMap<String, String> userProps = new HashMap<>();
    userProps.put(Configuration.PROXY_HOST, "localhost");
    userProps.put(Configuration.PROXY_PORT, "53128");
    userProps.put(Configuration.BOT_USERNAME, "TelIkka");
    userProps.put(Configuration.BOT_TOKEN, "298315362:AAEZMVxS0x6EEFTtEO8RJrSKQzl3LT0CziE");
    botManager.startBotSession("test", userProps, serviceProps);
  }
}
