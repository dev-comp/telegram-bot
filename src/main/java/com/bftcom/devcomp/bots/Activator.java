package com.bftcom.devcomp.bots;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.BotSession;

/**
 * This class implements a simple bundle that utilizes the OSGi
 * framework's event mechanism to listen for service events. Upon
 * receiving a service event, it prints out the event's details.
 **/
public class Activator implements BundleActivator, ServiceListener {
  public static final Logger logger = LoggerFactory.getLogger(Activator.class);

  BotSession botSession;

  /**
   * Implements BundleActivator.start(). Prints
   * a message and adds itself to the bundle context as a service
   * listener.
   *
   * @param context the framework context for the bundle.
   **/
  public void start(BundleContext context) {
    logger.info("Starting to listen for service events.");
    context.addServiceListener(this);

    TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
    try {
      EchoBot bot = new EchoBot();
      botSession = telegramBotsApi.registerBot(bot);
    } catch (TelegramApiException e) {
//      BotLogger.error("ERROR:", e);
    }
  }

  /**
   * Implements BundleActivator.stop(). Prints
   * a message and removes itself from the bundle context as a
   * service listener.
   *
   * @param context the framework context for the bundle.
   **/
  public void stop(BundleContext context) {
    context.removeServiceListener(this);
    logger.info("Stopped listening for service events.");
    //    EchoBotApp.main(new String[]{""});

    if (botSession!= null) {
      logger.info(EchoBotApp.class.getSimpleName() + " is shut down");
      botSession.close();
    }

    // Note: It is not required that we remove the listener here,
    // since the framework will do it automatically anyway.
  }

  /**
   * Implements ServiceListener.serviceChanged().
   * Prints the details of any service event from the framework.
   *
   * @param event the fired service event.
   **/
  public void serviceChanged(ServiceEvent event) {
    String[] objectClass = (String[]) event.getServiceReference().getProperty("objectClass");

    if (event.getType() == ServiceEvent.REGISTERED) {
      System.out.println("Ex1: Service of type " + objectClass[0] + " registered.");
    } else if (event.getType() == ServiceEvent.UNREGISTERING) {
      System.out.println("Ex1: Service of type " + objectClass[0] + " unregistered.");
    } else if (event.getType() == ServiceEvent.MODIFIED) {
      System.out.println("Ex1: Service of type " + objectClass[0] + " modified.");
    }
  }
}
