package com.bftcom.devcomp.bots;

/**
 * Константы
 */
public interface BotConst {
    // Префиксы очередей
    String QUEUE_SERVICE_PREFIX = "SERVICE_QUEUE_";   // Очереди, которые слушает сервис
    String QUEUE_ADAPTER_PREFIX = "ADAPTER_QUEUE_";   // Очереди, которые слушают адаптеры
    String QUEUE_ENTRY_PREFIX = "ENTRY_QUEUE_";       // Очереди, которые слушают экземпляры адаптеров

    // Имена системных пропертей, передаваемых в сообщении
    String PROP_ADAPTER_NAME = "ENTRY_NAME_";                // Свойство, определяющие имя адаптера
    String PROP_ENTRY_NAME = "ENTRY_NAME_";                  // Свойство, определяющие имя экземпляра адаптера
}
