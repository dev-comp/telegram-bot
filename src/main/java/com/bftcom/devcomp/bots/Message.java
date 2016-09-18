package com.bftcom.devcomp.bots;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * todo@shapoval add class description
 * <p>
 * date: 18.09.2016
 *
 * @author p.shapoval
 */
public class Message {

  private String guid;
  private String type;//request,response,info
  private String requestGuid;
  private String responseGuid;
  private String command;
  private Map<String, String> properties;

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getRequestGuid() {
    return requestGuid;
  }

  public void setRequestGuid(String requestGuid) {
    this.requestGuid = requestGuid;
  }

  public String getResponseGuid() {
    return responseGuid;
  }

  public void setResponseGuid(String responseGuid) {
    this.responseGuid = responseGuid;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  //tmp for fast test
  public static void main(String[] args) {
    ObjectMapper mapper = new ObjectMapper();
    Message obj = new Message();
    HashMap<String, String> config = new HashMap<>();
    config.put("test", "t");
    config.put("t", "tttt");
    obj.setProperties(config);

    obj.setGuid(UUID.randomUUID().toString());

    try {
      String jsonInString = mapper.writeValueAsString(obj);
      System.out.println(jsonInString);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
