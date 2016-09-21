package com.bftcom.devcomp.bots;

/**
 * Набор команд для адаптеров и их реализаций
 */
public enum BotCommand {
  SERVICE_GET_ACTIVE_ENTRIES,     // команда сервису от адаптера на предоставление списка активных экземпляров ботов
  SERVICE_PROCESS_ENTRY_MESSAGE,  // команда сервису на обработку сообщения от экземпляра бота (проброску клиенту)
  ADAPTER_START_ENTRY,            // команда адаптеру на запуск экземпляра бота
  ADAPTER_STOP_ENTRY,             // команда адаптеру на остановку экземпляра бота
  ADAPTER_STOP_ALL_ENTRIES        // команда адаптеру на остановку всех экземпляров его ботов
}
