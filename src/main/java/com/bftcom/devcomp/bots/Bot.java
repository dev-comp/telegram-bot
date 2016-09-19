package com.bftcom.devcomp.bots;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.BotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ikka
 * @date: 10.09.2016.
 */
public class Bot extends TelegramLongPollingBot {
  @SuppressWarnings("PackageAccessibility")
  private static Logger logger = LoggerFactory.getLogger(Bot.class);
  private Channel inChannel;
  private Channel outChannel;
  @SuppressWarnings("PackageAccessibility")
  private static ObjectMapper mapper = new ObjectMapper();
  private String outQueueName;
  private String inQueueName;

  public Bot(BotOptions options) {
    super(options);
  }
  
  

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();
      logger.debug("Message received: " + update);

      if (message.hasText()) {
        try {
          inChannel.basicPublish("", inQueueName, null, message.getText().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

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

  public void setInChannel(Channel inChannel) {
    this.inChannel = inChannel;
    
  }

  public Channel getInChannel() {
    return inChannel;
  }

  public void setOutChannel(Channel outChannel) {
    this.outChannel = outChannel;
    Channel channel = outChannel;
    String[] _consumerTag = new String[1];
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {

        String sMsg = new String(body, StandardCharsets.UTF_8);
        com.bftcom.devcomp.bots.Message message = mapper.readValue(sMsg, com.bftcom.devcomp.bots.Message.class);
        logger.info(" [-] Received message in OUT_QUEUE'" + message + "'");

        SendMessage sendMessageRequest = new SendMessage();
        
//        message.getChatId().toString()
        sendMessageRequest.setChatId(""); //who should get from the message the sender that sent it.
        sendMessageRequest.setText(message.getUserProperties().get("body"));

        try {
          sendMessage(sendMessageRequest); //at the end, so some magic and send the message ;)
        } catch (TelegramApiException e) {
          logger.error("", e);
        }
        
      }
    };
    try {
      _consumerTag[0] = channel.basicConsume(QueuesConfiguration.MANAGEMENT_QUEUE, true, consumer);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public Channel getOutChannel() {
    return outChannel;
  }


  public void setOutQueueName(String outQueueName) {
    this.outQueueName = outQueueName;
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
}
