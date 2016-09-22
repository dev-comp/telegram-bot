package com.bftcom.devcomp.bots.queues;

import com.bftcom.devcomp.api.IBotConst;
import com.bftcom.devcomp.bots.Bot;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Потребитель очереди сообщений для ботов
 * date: 22.09.2016
 *
 * @author p.shapoval
 */
public class BotQueueConsumer extends AbstractDefaultConsumer {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(BotQueueConsumer.class);
  private final Bot bot;

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   *
   * @param channel the channel to which this consumer is attached
   */
  public BotQueueConsumer(Channel channel, Bot bot) throws IOException {
    super(channel);
    this.bot = bot;
    channel.basicConsume(bot.getOutQueueName(), true, this);
  }

  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    String msg = new String(body, StandardCharsets.UTF_8);
    com.bftcom.devcomp.api.Message m = mapper.readValue(msg, com.bftcom.devcomp.api.Message.class);
    logger.info(" [-] Received message in OUT_QUEUE'" + m + "'");

    SendMessage sendMessageRequest = new SendMessage();
    sendMessageRequest.setChatId(m.getServiceProperties().get("chatId")); //a chatId to send the message to
    sendMessageRequest.setText(m.getUserProperties().get(IBotConst.PROP_BODY_TEXT));

    try {
      bot.sendMessage(sendMessageRequest);
    } catch (TelegramApiException e) {
      logger.error("", e);
    }
  }
}
