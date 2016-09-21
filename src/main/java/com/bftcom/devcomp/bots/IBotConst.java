package com.bftcom.devcomp.bots;

/**
 * Константы
 */
public interface IBotConst {
    // Префиксы очередей
    String QUEUE_SERVICE_PREFIX = "SERVICE_QUEUE_";   // Очереди, которые слушает сервис
    String QUEUE_ADAPTER_PREFIX = "ADAPTER_QUEUE_";   // Очереди, которые слушают адаптеры
    String QUEUE_BOT_PREFIX = "BOT_QUEUE_";       // Очереди, которые слушают экземпляры адаптеров

    // Имена системных пропертей, передаваемых в сообщении
    String PROP_ADAPTER_NAME = "ADAPTER_NAME";                // Свойство, определяющие имя адаптера
    String PROP_BOT_NAME = "BOT_NAME";                    // Свойство, определяющие имя экземпляра адаптера
    String PROP_USER_NAME = "USER_NAME";                      // Свойство, определяющие имя пользователя

    // Имена пользователских пропертей, передаваемых в сообщении
    String PROP_BODY_TEXT = "BODY_TEXT";                // Свойство, определяющие текст, передаваемый от бота и обратно (текст сообщения)

}
