# Адаптер для работы с Telegram API и Bot Service Platform (BSP)

Позволяет работать с IM Telegram и передавать/принимать ответы от BSP в формате JSON по протоколу AMPQ через его реализацию RabbitMQ. 

Сборка
===
mvn clean package bundle:bundle

Результирующий jar файл инсталируется в OSGI контейнер (например Apache Felix, JBOSGI) и должен быть им запущен. Собранный bundle (jar файл) может быть установлен в OSGI контейнер поддерживающий спецификацию OSGI R5 и OSGI R6. Финальная сборка была протестирована в Apache Felix version 5.0.4 и в JBSOGI version 2.5.2 и не требует никаких зависимостей. Запущенный адаптер получает команды от BSP и выполняет их. Доступные команды для адаптера

- Запустить бота
- Остановить бота
- Остановить бота
- Получить список активных ботов

Адаптер работает с BSP, используя очереди сервера обмена сообщениями RabbitMQ version 3.3.5. Через очереди сообщений боты принимают данные от BSP и отправляют данные BSP. Адаптер делает прозрачным для BSP протоколы и API различных мессенджеров. Адаптер позволяет не прерывать работу сервиса, в том числе и при необходимости обновления самого адаптера. Пользователь BSP может выполнить обновление и переход на новую версию адаптера без остановки самого адаптера (см. документацию об открытом стандарте OSGI). Т.е. по сути является плагином для BSP. Так же Adapter работает как обычное Java приложение. Для отладки удобнее запускать приложение как Java приложение. В промышленной среде, где требуется наибольшая степень безопасности запуск адаптера лучше делать в OSGI контейнере.

Вероятно, у кого-то возникают вопросы о рациональности использования OSGI, но в сложной среде модульность имеет особое значение. В первую очередь, избежание конфликтов версий классов в зависимых библиотек. В простом проекте OSGI будет излишним, но в расширяемом крупном проекте облегчит жизнь разработчикам, существенно понизит время недоступности сервисов.
