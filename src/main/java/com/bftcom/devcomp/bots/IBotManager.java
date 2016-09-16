package com.bftcom.devcomp.bots;

/**
 * @author ikka
 * @date: 16.09.2016.
 */
public interface IBotManager {
  boolean startBotSession(String string, String proxyHost, Integer proxyPort);

  boolean stopBotSession(String string);
}
