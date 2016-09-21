package com.bftcom.devcomp.bots;

import com.bftcom.devcomp.api.IBotManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * This class implements a simple bundle that utilizes the OSGi
 * framework's event mechanism to listen for service events. Upon
 * receiving a service event, it prints out the event's details.
 **/
public class Activator implements BundleActivator, ServiceListener, QueuesConfiguration {
  @SuppressWarnings("PackageAccessibility")
  public static final Logger logger = LoggerFactory.getLogger(Activator.class);

  @SuppressWarnings("PackageAccessibility")
  ObjectMapper mapper = new ObjectMapper();


  BundleContext context;
  IBotManager botManager;

  private ConnectionFactory factory;
  private Connection connection;
  private Channel channel;
//  ServiceRegistration<?> serviceRegistration;


  /**
   * Implements BundleActivator.start(). Prints
   * a message and adds itself to the bundle context as a service
   * listener.
   *
   * @param context the framework context for the bundle.
   **/
  public void start(BundleContext context) {
    logger.info("-------------------------------------------------------------------------------------------------");
    logger.info("-------------------------------------------------------------------------------------------------");
    logger.info("-------------------------------------------------------------------------------------------------");
    logger.info("Bundle started. Starting to listen for service events.");
    logger.info("-------------------------------------------------------------------------------------------------");
    logger.info("-------------------------------------------------------------------------------------------------");
    logger.info("-------------------------------------------------------------------------------------------------");

/*
    botManager = new BotManager();
*/
    context.addServiceListener(this);

    //failed attempt to work through interfaces directly.  
    //serviceRegistration = context.registerService(IBotManager.class.getName(), botManager, null);
  }

  /**
   * Implements BundleActivator.stop(). Prints
   * a message and removes itself from the bundle context as a
   * service listener.
   *
   * @param context the framework context for the bundle.
   **/
  public void stop(BundleContext context) {
    // Note: It is not required that we remove the listener here,
    // since the framework will do it automatically anyway.
    context.removeServiceListener(this);
    //serviceRegistration.unregister();
    botManager.stopAllBotSessions();
    logger.info("Stopped listening for service events.");

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
