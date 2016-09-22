package com.bftcom.devcomp.bots.queues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;

/**
 * date: 22.09.2016
 * @author p.shapoval
 */
abstract public class AbstractDefaultConsumer  extends DefaultConsumer {
  @SuppressWarnings("PackageAccessibility")
  public static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   *
   * @param channel the channel to which this consumer is attached
   */
  public AbstractDefaultConsumer(Channel channel) {
    super(channel);
  }
}
