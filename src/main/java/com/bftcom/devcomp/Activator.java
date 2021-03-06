package com.bftcom.devcomp;

import com.bftcom.devcomp.adapters.TelegramAdapter;
import com.bftcom.devcomp.api.IMessengerAdapter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a simple bundle that utilizes the OSGi
 * framework's event mechanism to listen for service events. Upon
 * receiving a service event, it prints out the event's details.
 **/
public class Activator implements BundleActivator, ServiceListener {
  @SuppressWarnings("PackageAccessibility")
  private static final Logger logger = LoggerFactory.getLogger(Activator.class);

  private IMessengerAdapter messengerAdapter;
  //ServiceRegistration<?> serviceRegistration;


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
    logger.info("-----------------   Bundle started. Starting to listen for service events.   --------------------");
    logger.info(context.getBundle().getSymbolicName());
    logger.info("-------------------------------------------------------------------------------------------------");
    logger.info("-------------------------------------------------------------------------------------------------");


    messengerAdapter = new TelegramAdapter();
    context.addServiceListener(this);

    //failed attempt to work through interfaces directly.  
    //serviceRegistration = context.registerService(IBotManager.class.getName(), messengerAdapter, null);
  }

  /**
   * Implements BundleActivator.stop(). Prints
   * a message and removes itself from the bundle context as a
   * service listener.
   *
   * @param context the framework context for the bundle.
   **/
  public void stop(BundleContext context) {
    // Note: It is not required that we remove the listener here, since the framework will do it automatically anyway.
    context.removeServiceListener(this);
    //serviceRegistration.unregister();
    messengerAdapter.stopAllBotSessions();
    logger.info("Stopped listening for service events.");

  }

  /**
   * Prints the details of any service event from the framework.
   * @param event the fired service event.
   **/
  public void serviceChanged(ServiceEvent event) {
    String[] objectClass = (String[]) event.getServiceReference().getProperty("objectClass");
    if (event.getType() == ServiceEvent.REGISTERED) {
      logger.info("Service of type " + objectClass[0] + " registered.");
    } else if (event.getType() == ServiceEvent.UNREGISTERING) {
      logger.info("Service of type " + objectClass[0] + " unregistered.");
    } else if (event.getType() == ServiceEvent.MODIFIED) {
      logger.info("Service of type " + objectClass[0] + " modified.");
    }
  }
}
