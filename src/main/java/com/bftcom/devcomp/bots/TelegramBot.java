package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.BotCommand;
import com.bftcom.devcomp.api.IBot;
import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.queues.BotQueueConsumer;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.BotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.IOException;
import java.util.Map;

/**
 * Бот для Telegram
 *
 * @author ikka
 * @date: 10.09.2016.
 */
@SuppressWarnings("PackageAccessibility")
public class TelegramBot extends TelegramLongPollingBot implements IBot {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

  private String username;
  private String token;
  private Channel inChannel;
  private Channel outChannel;//
  private String outQueueName;
  private String inQueueName;
  private String name;

  public TelegramBot(BotOptions botOptions, String name, String username, String token) {
    super(botOptions);
    this.name = name;
    this.username = username;
    this.token = token;
  }

  /**
   * Handle incoming messages from IM users
   *
   * @param update Update received
   */
  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      logger.info("Message received: " + update);
      Message message = update.getMessage();

      if (message.hasText()) {
        com.bftcom.devcomp.api.Message msgToForward = new com.bftcom.devcomp.api.Message();
        msgToForward.setCommand(BotCommand.SERVICE_PROCESS_BOT_MESSAGE);
        Map<String, String> userProperties = msgToForward.getUserProperties();
        Map<String, String> serviceProperties = msgToForward.getServiceProperties();
        userProperties.put(IBotConst.PROP_BODY_TEXT, message.getText());
        serviceProperties.put(IBotConst.PROP_BOT_NAME, getName());
        serviceProperties.put(IBotConst.PROP_USER_NAME, message.getFrom().getFirstName());
        serviceProperties.put("chatId", String.valueOf(message.getChatId()));

        handleUserIncomingData(msgToForward);
      }

      //todo tmp echo test  
      if (message.hasText()) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId().toString());
        sendMessageRequest.setText("DEBUG: " + message.getText());
        try {
          sendMessage(sendMessageRequest);
        } catch (TelegramApiException e) {
          logger.error("", e);
        }
      }
    }
  }

  @Override
  public String getBotUsername() {
    return username;
  }

  @Override
  public String getBotToken() {
    return token;
  }

  public void setInChannel(Channel inChannel) {
    this.inChannel = inChannel;
  }

  public Channel getInChannel() {
    return inChannel;
  }

  public String getOutQueueName() {
    return outQueueName;
  }

  public void setInQueueName(String inQueueName) {
    this.inQueueName = inQueueName;
  }

  public String getInQueueName() {
    return inQueueName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Channel getOutChannel() {
    return outChannel;
  }

  public void setOutChannel(Channel outChannel) {
    this.outChannel = outChannel;
    try {
      new BotQueueConsumer(outChannel, this);
    } catch (IOException e) {
      logger.error("", e);
    }
  }

  public void setOutQueueName(String outQueueName) {
    this.outQueueName = outQueueName;
  }

  @Override
  public void sendMessage(Object o) {
    if (o instanceof SendMessage) {
      try {
        sendMessage((SendMessage) o);
      } catch (TelegramApiException e) {
        logger.error("", e);
      }
    }
  }

  @Override
  public void handleDelivery(com.bftcom.devcomp.api.Message m) {
    SendMessage sendMessageRequest = new SendMessage();
    sendMessageRequest.setChatId(m.getServiceProperties().get("chatId")); //a chatId to send the message to
    sendMessageRequest.setText(m.getUserProperties().get(IBotConst.PROP_BODY_TEXT));
    sendMessage((Object) sendMessageRequest);
  }
}
