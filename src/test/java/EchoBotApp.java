import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EchoBotApp {
  private static final Logger logger = LoggerFactory.getLogger(EchoBotApp.class);

  public static void main(String[] args) {
//    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();// assume SLF4J is bound to logback in the current environment
//    StatusPrinter.print(lc);// print logback's internal status
//
//    BotSession botSession = null;
//    TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
//    try {
//      BotOptions botOptions = new BotOptions();
//      botOptions.setProxyHost("localhost");
//      botOptions.setProxyPort(53128);
//      EchoBot bot = new EchoBot(botOptions);
//      botSession = telegramBotsApi.registerBot(bot);
//    } catch (TelegramApiException e) {
//      BotLogger.error("ERROR:", e);
//    }
//
//    final BotSession finalBotSession = botSession;
//    Runtime.getRuntime().addShutdownHook(new Thread() {
//      @Override
//      public void run() {
//        if (finalBotSession != null) {
//          logger.info(EchoBotApp.class.getSimpleName() + " is shut down");
//          finalBotSession.close();
//        }
//      }
//    });
  }
}
