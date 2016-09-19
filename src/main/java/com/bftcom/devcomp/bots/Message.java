package com.bftcom.devcomp.bots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Класс, представляющий собой сообщение для бомена между сервисом и адаптерами/экземплярами ботов
 * date: 18.09.2016
 *
 * @author p.shapoval
 */
public class Message {

  private BotCommand command;
  private Map<String, String> userProperties;
  private Map<String, String> serviceProperties;

  public BotCommand getCommand() {
    return command;
  }

  public void setCommand(BotCommand command) {
    this.command = command;
  }

  public Map<String, String> getUserProperties() {
    return userProperties;
  }

  public void setUserProperties(Map<String, String> userProperties) {
    this.userProperties = userProperties;
  }

  public Map<String, String> getServiceProperties() {
    return serviceProperties;
  }

  public void setServiceProperties(Map<String, String> serviceProperties) {
    this.serviceProperties = serviceProperties;
  }

  @Override
  public String toString() {
    return "Message{" +
        "command=" + command +
        ", userProperties=" + userProperties +
        ", serviceProperties=" + serviceProperties +
        '}';
  }

  @SuppressWarnings("PackageAccessibility")
  public static void main(String[] args) {
    ObjectMapper objectMapper = new ObjectMapper();
    Message message = new Message();
    message.setCommand(BotCommand.ENTRY_PROCESS_MESSAGE);

    try {
      System.out.println(objectMapper.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
