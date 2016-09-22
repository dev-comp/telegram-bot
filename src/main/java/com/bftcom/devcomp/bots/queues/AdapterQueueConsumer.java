package com.bftcom.devcomp.bots.queues;

import com.bftcom.devcomp.api.BotCommand;
import com.bftcom.devcomp.api.Configuration;
import com.bftcom.devcomp.api.Message;
import com.bftcom.devcomp.bots.BotManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик сообщений полученный из очереди для Адаптера.
 * <p>
 * date: 22.09.2016
 *
 * @author p.shapoval
 */
public class AdapterQueueConsumer extends AbstractDefaultConsumer {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(AdapterQueueConsumer.class);
  private final BotManager botManager;

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   *
   * @param channel the channel to which this consumer is attached
   */
  public AdapterQueueConsumer(Channel channel, BotManager botManager, String queueName) throws IOException {
    super(channel);
    this.botManager = botManager;

    AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(queueName, false, false, false, null);//create if not yet created

    logger.debug("subscribing to the queue " + queueName);
    String[] _consumerTag = new String[1];
    _consumerTag[0] = getChannel().basicConsume(queueName, true, this);//subscribe
    getChannel().addShutdownListener(cause -> logger.info("shutting down channel " + declareOk.getQueue()));
    logger.debug("subscribed to the queue " + queueName + " with consumerTag = " + _consumerTag[0]);
  }

  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    String msg = new String(body, StandardCharsets.UTF_8);
    logger.debug(" [x] Received '" + msg + "'");

    Message m = mapper.readValue(msg, Message.class);
    BotCommand command = m.getCommand();
    logger.debug("Command " + command + " received");

    switch (command) {
      case ADAPTER_START_BOT:
        botManager.startBotSession(m.getUserProperties().get(Configuration.BOT_TOKEN), m.getUserProperties(), m.getServiceProperties());
        break;
      case ADAPTER_STOP_ALL_BOTS:
        botManager.stopAllBotSessions();
        break;
      case ADAPTER_STOP_BOT:
        botManager.stopBotSession(m.getUserProperties().get(Configuration.BOT_TOKEN));
        break;
    }
  }
}
