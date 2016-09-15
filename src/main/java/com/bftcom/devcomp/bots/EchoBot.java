package com.bftcom.devcomp.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 * @author ikka
 * @date: 10.09.2016.
 */
public class EchoBot extends TelegramLongPollingBot {
  private static Logger logger = LoggerFactory.getLogger(EchoBot.class);



  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();
      logger.debug("Message received: " + update);

      //check if the message has text. it could also contain for example a location ( message.hasLocation() )
      if (message.hasText()) {
        //create an object that contains the information to send back the message
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId().toString()); //who should get from the message the sender that sent it.
        sendMessageRequest.setText(message.getText());

        if (message.getText().equalsIgnoreCase("exit")) {
          logger.info("Bot is shutting down");
          System.exit(0);

        }

        try {
          sendMessage(sendMessageRequest); //at the end, so some magic and send the message ;)
        } catch (TelegramApiException e) {
          logger.error("", e);
        }
      }
    }
  }

  @Override
  public String getBotUsername() {
    return "BftDevCompEchoService";
  }

  @Override
  public String getBotToken() {
    return "299411168:AAFB8lD1_08mklizl_xwH93lIckjEHIpjCE";
  }
}
