Apache Kafka начала отказываться от использования Zookeeper начиная с версии 2.8.0, когда был анонсирован проект KRaft (Kafka Raft Metadata Mode).  
Однако, полное избавление от Zookeeper было реализовано только начиная с версии 3.0.0.  
Вот ключевые моменты:  
- Apache Kafka 2.8.0: Начало поддержки KRaft, но возможность работы с Zookeeper все еще сохранялась.  
- Apache Kafka 3.0.0: Полное избавление от Zookeeper для управления метаданными и использования KRaft в качестве основного метода.  
- KRaft предназначен для упрощения архитектуры Kafka, уменьшения сложности управления и улучшения масштабируемости и надежности системы.   

Однако, большинство развертываний Kafka на данный момент все еще используют Zookeeper, так как переход на KRaft требует значительных изменений и тестирования.  

>## Работа с Kafka в Docker
### Пример полного процесса создания топика в контейнере с именем kafka:
#### Вход в контейнер Kafka:
```shell
docker exec -it kafka /bin/bash
````
#### Создание топика:
```shell
kafka-topics.sh --create --topic your_topic_name --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
````
#### Проверка создания топика - Эта команда покажет список всех топиков в Kafka.
```shell
kafka-topics.sh --list --bootstrap-server localhost:9092
````
#### Альтернативный способ (без входа в контейнер)
```shell
docker-compose exec kafka kafka-topics.sh --create --topic your_topic_name --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
````
---
### Шаги для запуска консюмера из консоли:
#### Получите доступ к контейнеру Kafka:
```shell
docker exec -it kafka /bin/bash
```
#### Запустите консюмер
```shell
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic your_topic_name --from-beginning
```

`--from-beginning`: Опция для чтения сообщений с начала топика. Если вы не укажете эту опцию, консюмер начнет читать сообщения, поступившие после его запуска

#### Альтернативный способ (без входа в контейнер)
```shell
docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic your_topic_name --from-beginning  
```

Дополнительные параметры:  
- `--timeout-ms <milliseconds>`: Устанавливает время ожидания, после которого консюмер завершит работу, если не получено сообщений.  
- `--partition <partition>`: Указывает конкретную партицию для чтения
---
docker-compose.yml  
```dockerfile
version: '3'

services:
  kafka:
    image: wurstmeister/kafka:latest
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_LISTENER_NAME_PLAIN: INSIDE,OUTSIDE
      KAFKA_LISTENERS: INSIDE://0.0.0.0:9093,OUTSIDE://0.0.0.0:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
      KAFKA_LISTENER_SECURITY_PROTOCOL: PLAINTEXT
      KAFKA_LISTENER_NAMES: INSIDE,OUTSIDE
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      KAFKA_CONFLUENT_SUPPORT_METRICS_ENABLE: 'false'
      KAFKA_KRAFT_MODE: 'true'
      KAFKA_PROCESS_ROLES: broker
      KAFKA_CONFLUENT_SUPPORT_METRICS_ENABLE: 'false'
      KAFKA_BROKER_ID: 1
      KAFKA_LOG_RETENTION_HOURS: 168
    volumes:
      - kafka-data:/var/lib/kafka/data
    networks:
      - kafka-network

networks:
  kafka-network:
    driver: bridge

volumes:
  kafka-data:
```
---
```dockerfile
  kafka:
    image: 'nexus.datas-tech.com:445/datas-kraft-kafka:2.8.0'
    hostname: kafka1
    container_name: 'kafka1'
    restart: 'on-failure'
    environment:
      LISTENER_PORT: 9092
    ports:
      - "9092:9092"
```

>### Kafka Broker (Kafka Server | Kafka Node)
- прием сообщений
- хранение сообщений
- выдача сообщений
  Kafka используется свой сетевой протокол для обмена данными.



> ### Kafka Cluster

набор взаимодействующих между собой Kafka Brokers
- масштабирование
- репликация

Если хотим подключться к Kafka Cluster, нужно знать адрес и порт одного брокера. Далее адреса остальных брокеров запрашиваются автоматически.  
Каждый брокер – bootstrap сервер. Кажды брокер знает обо всех других брокерах, их топиках и партициях.

> ### Kafka Controller
мастер брокер который обеспечивает консистентность

> ### Kafka Message
Key-Value пары:
- ***Key*** -  ключ (опционально) используется для распределения сообщений по кластеру
- ***Value*** - содержимое сообщения - массив байт
- ***Timestamp*** - время сообщения
- ***Headers*** - набор key-value пар с пользовательскими аттрибутами сообщений

> ### Kafka Topic
поток данных для логически однотипных событий, которые образуют FIFO-очередь  
- При чтении Consumer сообщений из топика, сами сообщения не удаляются, что обеспечивает широковещательный режим - передачу сообщения нескольким Consumers

> ### Kafka Partitions
1. Что такое разделы (partitions)?

   - Разделы — это логические подгруппы внутри Kafka-топика. Каждый топик может быть разделён на несколько разделов.
   - Каждый раздел хранит подмножество данных топика и представляет собой упорядоченную, неизменяемую последовательность записей.
   - Каждому сообщению в разделе присваивается уникальный смещающий индекс (offset), который позволяет отслеживать позицию сообщений.

2. Зачем нужны разделы?
   - Масштабирование и производительность: Разделы позволяют параллельно обрабатывать данные. Каждый раздел может быть обработан отдельным консюмером, что повышает общую производительность.
   - Дистрибуция данных: Kafka распределяет разделы топика между брокерами, что позволяет эффективно использовать ресурсы кластера и повысить отказоустойчивость.

3. Как данные распределяются по разделам?
   - При отправке сообщения продюсером Kafka решает, в какой раздел поместить сообщение. Это может быть сделано:
     - По ключу (key): Если сообщение имеет ключ, Kafka использует хеш-функцию для определения, в какой раздел его отправить. Сообщения с одинаковым ключом всегда попадут в один и тот же раздел.
     - Без ключа: Если ключ не указан, Kafka распределяет сообщения по разделам с использованием стратегии round-robin или других алгоритмов.

4. Как разделы работают с консюмерами?
   - Группа консюмеров (consumer group) распределяет обработку разделов между собой. Каждый раздел может быть назначен только одному консюмеру внутри группы, что позволяет обеспечить горизонтальное масштабирование обработки данных.
   - Если количество консюмеров меньше количества разделов, некоторые консюмеры будут обрабатывать несколько разделов.

5. Отказоустойчивость и репликация:
   - Kafka поддерживает репликацию разделов для обеспечения отказоустойчивости. Каждый раздел может иметь несколько реплик, одна из которых является лидером. Только лидер обслуживает запросы на запись и чтение, а остальные реплики синхронизируются с ним.

6. Порядок сообщений:
   - Важно отметить, что порядок сообщений гарантируется только внутри одного раздела. Если у вас несколько разделов, сообщения с одинаковым ключом будут находиться в одном разделе, что обеспечивает их упорядоченность.

- Порядок получения сообщений из нескольких позиций может НЕ совпадать с порядком их записи в топик.  
- В рамках одной партиции порядок сообщений гарантируется - в каком порядке сообщения были отправлены в партицию, в таком порядке они будут обработаны.  
- Партиции топика распределяются по кластеру между нодами/брокерами (балансировка).  
- Можно добавлять партиции.  
- Убрать партиции из топика можно только через пересоздание топика.

> ### Storage for Kafka Topic  

![Storage for Kafka Topic.png](src/main/resources/image/Storage%20for%20Kafka%20Topic.png)

Данные топиков хранятся в обычных log-файлах в файловой системе брокера в папках под каждую партицию (имя подпапки <log.dirs>/<topic-name>-<partition-number>/)
```kotlin
/var/lib/kafka/data/orders-0/
/var/lib/kafka/data/orders-1/
```

Broker File System:
```
./logs
    ./topicName-0
        00000000000000000000.log
        00000000000000000000.index
        00000000000000000000.timeindex
        00000000000000012345.log
        00000000000000012345.index
        00000000000000012345.timeindex   
        ... 
    ./topicName-1
    ...
    ./topicName-N
```

Содержит файлы `*.log`, `*.index` и `*.timeindex` (эти файлы называются сегментами)  

*.log - файл сегмента данных (имя формируется из номера первого offset в этом файле).  
Имеет лимит по размеру, по умолчанию 1 Гб, при превышении лимита создается следующий сегмент.

| offset | position | timestamp     | message     |
|--------|----------|---------------|-------------|
| 0      | 0        | 1616418669000 | Сообщение 1 |
| 1      | 67       | 1616625436000 | Сообщение 2 |

- offset (номер сообщения в партиции)
- position (позиция сообщения в файле в байтах)
- timestamp
- message

*.index - маппинг offset на position

| offset | position |
|--------|----------|
| 0      | 0        |
| 1      | 67       |



*.timeindex - маппинг timestamp на offset


| timestamp     | offset  |
|---------------|---------|
| 1616418669000 | 0       |
| 1616625436000 | 1       |

**Message Key:** Key – часть сообщения, может быть null; в качестве key можно использовать id пользователя
- Если null, то выбирается partition по round robin (по очереди, по кругу; все партиции равномерно заполняются)
- Если не null, то все сообщения с этим ключом пишутся всегда в один partition (случай, когда события от одного пользователя нужно обрабатывать по очереди – events одного клиента, лог одного сервиса, изменения из одного источника…)

**Удаление сообщений**
- Прямая операция удаления данных из топика отсутствует.  
- Данные автоматически удаляются по TTL (time-to-live) - удаляются целиком сегменты у которых segment timestamp (максимальный timestamp в сегменте) устарел

>### Data Replication  
**replication-factor** - настройка топика  
Если > 1, то копии одной и той же партиции создаются в разных нодах/брокерах (реплики одной партиции НЕ МОГУТ находиться на одном брокере)

**Leader реплика** - часть master-slave системы репликации   
- Одна из реплик назначается Kafka Controller главной (leader) репликой, остальные follower.  
- Операции чтения и записи производятся ТОЛЬКО с leader-репликой. (producer -> [leader] -> consumer)
- Follower-реплики переодически опрашивают leader для получения новых данных
- Возможна проблема несбалансированости (решается конфигурацией): когда все или большинство leader-реплик оказывается в одной ноде и вся нагрузка по операциям записи-чтения ложится на эту ноду.

|             | Broker 0     | Broker 1     | Broker 2     |
|-------------|--------------|--------------|--------------|
| Partition 0 | 0 - Leader   | 0 - Follower | 0 - Follower |
| Partition 1 | 1 - Follower | 1 - Leader   | 1 - Follower |
| Partition 2 | 2 - Follower | 2 - Follower | 2 - Leader   |

**Синхронизация данных между leader-репликой и followers:**  
- fallowers периодически опрашивают leader-реплику на предмет наличия новых данных для синхронизации.
- Возможно отставание данных в fallower-репликах и даже потеря данных при отключении ноды с leader-репликой.
- Решение: In-sync replicas (ISR) (min.insync.replicas). В fallower-реплики, которые помечены как ISR, данные записываются одновременно/синхронно с leader-репликой (`min.insync.replicas`).  
ISR fallower - первый кандидат на leader-реплику, если текущая leader-реплика выйдет из строя.

> ### Kafka Producer  

UML-диаграмма публикации сообщений
![UML-диаграмма публикации сообщений.png](src/main/resources/image/UML-диаграмма%20публикации%20сообщений.png)

Гарантия доставки:
- `acks=0` - producer не ждет подтверждения отправки сообщения от брокера (сообщения могут теряться)
- `acks=1` - producer ждет подтверждения отправки только от leader-реплики (сообщения могут теряться если брокер с leader-репликой вышел из строя до реплицирования сообщений)
- `acks=-1 (all)` - producer ждет подтверждения от всех ISR-реплик, включая leader (сообщения не теряются)

**Producer Send Message**
1. **fetch metadata** - получение данных о кластере, топиках и leader-репликах (блокирующая/синхронная операция)
2. **serialize message** - сериализация сообщения в нужный формат (key.serializer, value.serializer)
3. **define partition** - выбираем партицию для сообщения
   - `explicit partition` - явное указание партиции
   - `round-robin (by default)` - случайный выбор партиции
   - `key-defined (key_hash % n)` - партиция определяется по ключу
4. **compress message**
   - `compression.type = none | gzip | snappy | lz4 | zstd`
5. **accumulate batch** - объединение сообщений в пакет перед отправкой в брокер. Батч накапливается до достижения одного из двух условий:
   - `batch.size` - суммарный, в моменте, размер формирующихся пакетов для разных партиций одного брокера (16 Кб по умолчанию),
   - `linger.ms` - таймаут по истечению которого накопление сообщений прекращается и пакет отправляется
6. **send** - отправка пакетов в партиции брокеров.

> ### Kafka Consumer

![UML-диаграмма последовательности потребления сообщений из Kafka.png](src/main/resources/image/UML-диаграмма%20последовательности%20потребления%20сообщений%20из%20Kafka.png)

 Читает только из leader-реплики партиции  

**Сетевые соединения:** Kafka консюмер поддерживает постоянное соединение с брокером, а не создаёт новое соединение для каждого запроса.  
**Протокол:** Kafka использует свой собственный протокол, который оптимизирован для высокоскоростного обмена данными и поддерживает асинхронное взаимодействие.

Количество активных `Consumer` в одной группе, которые читают топик, не может быть больше количества партиций в топике.  
Консюмеры из одной группы не могут параллельно читать сообщения из одной и той же партиции.  
В противном случае (например, партиция одна, а консюмеров в группе 2):
1. Kafka автоматически назначит единственную партицию одному из двух консюмеров в группе.
2. Этот консюмер будет получать и обрабатывать все сообщения из этой партиции.
3. Второй консюмер в группе останется без назначенной партиции и не будет получать никаких сообщений, пока первый консюмер активен.
Таким образом, для максимального использования всех консюмеров в группе рекомендуется иметь количество партиций не меньше, чем количество консюмеров в группе. 
Это позволит каждому консюмеру получать сообщения и обрабатывать их параллельно.
Если первый консюмер отключится или перестанет быть активным, Kafka автоматически перераспределит партицию второму консюмеру, который начнет получать сообщения.
poll message:
4. fetch metadata - получение данных о кластере, топиках и leader-репликах (блокирующая/синхронная операция)
5. Подключение к Leader-репликам всех партиций топика.
6. poll - опрос

**Consumer Group (group.id)** - группа сonsumers для параллельного чтения собщений из топика - каждый consumer читает свои партиции.

**Kafka Consumer Offset**  
В Kafka есть отдельный топик `__consumer_offsets`, где хранятся коммиты - информация о смещении последнего прочитанного сообщения из указанного топика-партиции для указанной группы Consumers

| Field     | Value                      |
|-----------|----------------------------|
| Partition | NameTopic/NumberPartition  |
| Group     | Group Id                   |
| Offset    | offset value               |

Таким образом фиксируется последнее сообщение в партиции, которое было обработано одним из консюмеров группы, что позволяет избежать повторного чтения сообщений разными консюмерами одной и той же группы.

Порядок чтения сообщений: с начала, с конца (по умолчанию)
auto.offset.reset = earliest | latest | none

**Типы коммитов:**
- **Auto commit (at most once)** - по умолчанию: происходит при получении пакета сообщений. Если Consumer не смог их все обработать, то часть сообщений может быть утрачена,
так как они уже заккомичены (значение offset сдвинули) и повторно их нельзя прочитать
  - `enable.auto.commit = true | false`
  - `auto.commit.interval.ms = 5000`
- **Manual commit (at least once)** - используется в Spring Kafka: коммит отправляется только после успешной обработки всех сообщений пакета путем вызова метода `acknowledge` (в этом случае возможно дублированная обработка сообщений)
- **Custom offset management (but exactly once)** - реализуем свое хранилище offsets и четаем данные с указанного нами offset


> ### Join Kafka Stream

Kafka Input Stream 1 --> Consumer 1 -> Storage -> Producer 1 -> Kafka Output Stream 1
<|>
Kafka Input Stream 2 --> Consumer 2 -> Storage -> Producer 2 -> Kafka Output Stream 2


> ### Kafka Connection

> ### Kafka Streams

> ### KSQL

> ### Miror Maker

> ### Transactions

> ### λ - архитектура

> ### Κ - архитектура

Клиентская API библиотека для обработки и анализа данных, хранящихся в Kafka.

---

> ## Spring Kafka

Использует под капотом библиотеку org.apache.kafka.clients

1. Обработка аннотации `@KafkaListener`:
   Когда вы используете аннотацию `@KafkaListener`, она сначала обрабатывается `KafkaListenerAnnotationBeanPostProcessor`, который регистрирует обработчики сообщений и создает необходимые контейнеры для потребителей (consumers).

2. Создание контейнера:
   `KafkaListenerAnnotationBeanPostProcessor` создает и настраивает объект `KafkaMessageListenerContainer` или его подкласс, такой как `ConcurrentMessageListenerContainer`, для каждого слушателя, заданного через `@KafkaListener`. Этот контейнер управляет созданием и запуском потока для обработки сообщений.

3. Внутри контейнера:
   Внутри контейнера `KafkaMessageListenerContainer` создается экземпляр внутреннего класса `ListenerConsumer`. Этот класс является основным рабочим компонентом, который выполняет цикл опроса сообщений через метод poll() и их последующую обработку.

4. Когда Spring Boot запускается, он автоматически конфигурирует `KafkaTemplate` и консьюмеры  при создании соответствующих бинов, используя доступные параметры конфигурации:
   - `KafkaTemplate` будет использовать параметры из `spring.kafka.producer` для отправки сообщений.
   - `@KafkaListener` будет использовать параметры из `spring.kafka.consumer` для получения сообщений.
 
5. Запуск контейнера и вызов run():
   Когда контейнер запускается (обычно это происходит при старте приложения), он создает и запускает поток, в котором выполняется метод run() класса ListenerConsumer. Этот метод инициализирует бесконечный цикл, который регулярно вызывает poll() на KafkaConsumer и обрабатывает сообщения.
```java
while (isRunning()) {
    try {
        ConsumerRecords<K, V> records = consumer.poll(pollTimeout);
        // Обработка записей
        invokeListener(records);
    } catch (Exception e) {
        // Обработка ошибок
    }
}
```

Метод, аннотированный @KafkaListener, вызывается автоматически, когда в топике появляются новые сообщения.
Вот как это происходит:

Дефиниции параметров Kafka Producer их значения по умолчанию и описание можно найти в классе org.apache.kafka.clients.producer.ProducerConfig
Дефиниции параметров Kafka Consumer их значения по умолчанию и описание можно найти в классе org.apache.kafka.clients.consumer.ConsumerConfig


1. Конфигурация слушателя:

В Spring Kafka вы настраиваете слушателя, используя аннотацию @KafkaListener на методе.
Эта аннотация указывает, какой топик слушать и другие параметры, такие как группа консюмеров.
```java
import org.springframework.kafka.annotation.KafkaListener;

@KafkaListener(topics = "topic1", groupId = "group_id")
public void listen(String message) {
    System.out.println("Received message: " + message);
}
```

2. Создание контейнера для слушателя:

Spring Kafka автоматически создает контейнер для каждого метода, аннотированного @KafkaListener.
Этот контейнер управляет жизненным циклом консюмера Kafka и выполняет основные задачи по обработке сообщений.

3. Опрос сообщений:

Внутри контейнера работает фоновый поток, который периодически вызывает метод poll Kafka консюмера для получения новых сообщений из брокера Kafka.
Интервал между вызовами poll задается свойством pollTimeout, как обсуждалось ранее.
```java
@Bean
 public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
     ConcurrentKafkaListenerContainerFactory<String, String> factory =
             new ConcurrentKafkaListenerContainerFactory<>();
     factory.setConsumerFactory(consumerFactory());
     factory.getContainerProperties().setPollTimeout(3000); // Установите здесь желаемый pollTimeout в миллисекундах
     return factory;
 }
```

4. Обработка сообщений:

Когда в результате вызова poll получены новые сообщения, контейнер передает их методу, аннотированному @KafkaListener.
Контейнер автоматически десериализует сообщения и вызывает метод с этими сообщениями в качестве аргументов.
Таким образом, метод listen в примере выше будет автоматически вызван контейнером, как только появится новое сообщение в топике topic1.
Контейнер управляет вызовом метода, гарантируя, что он вызывается при поступлении новых сообщений.
---
**KafkaConfigurationUtils**

Обрабатываются только те сообщения, которые были доступны на момент вызова метода poll.  
Новые сообщения, поступившие после этого (пока консюмер обрабатывает текущие сообщения), будут обработаны в следующих вызовах метода poll.

Параметр idle-between-polls в Spring Kafka время ожидания между завершением обработки всех сообщений, полученных при первом вызове poll, и следующим (вторым) вызовом poll.
То есть, это пауза, которую консюмер делает после завершения обработки текущей партии сообщений перед тем, как снова вызвать метод poll для получения новой партии сообщений.
Этот параметр позволяет регулировать частоту опроса Kafka топика консюмером.
Этот параметр может быть полезен для управления нагрузкой и эффективностью использования ресурсов, особенно когда топик часто пуст или содержит мало сообщений.
factory.getContainerProperties().setPollTimeout(3000);
factory.getContainerProperties().setIdleBetweenPolls(1000L);
Описание параметров
- pollTimeout: Время ожидания ответа от вызова poll. Если сообщений нет, консюмер ждет указанное время (в миллисекундах) перед возвратом пустого результата.
- idle-between-polls: Время ожидания между двумя последовательными вызовами poll, если в предыдущем вызове не было получено никаких сообщений.

Пример использования
С помощью приведенной конфигурации консюмер будет делать следующее:
- Выполнит вызов poll и будет ждать до 3000 миллисекунд для получения сообщений.
- Если сообщений нет, консюмер сделает паузу на 1000 миллисекунд перед следующим вызовом poll.
Это позволяет уменьшить нагрузку на систему, когда в топике мало или совсем нет сообщений.

Используйте динамическую настройку idleBetweenPolls:

В зависимости от загрузки и наличия сообщений вы можете динамически менять значение idleBetweenPolls.
Во время плотного потока сообщений уменьшайте значение idleBetweenPolls для более быстрого опроса, а когда поток утихнет, увеличивайте значение для снижения нагрузки.

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private AtomicLong idleBetweenPolls = new AtomicLong(1000); // Начальное значение 1 секунда

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 900000); // 15 минут
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // Ограничение количества записей
        return new DefaultKafkaConsumerFactory<>(props);
    } 	

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setPollTimeout(3000); // 3 секунды

        // Использование AtomicLong для динамической настройки idleBetweenPolls
        factory.getContainerProperties().setIdleBetweenPolls(idleBetweenPolls.get());

        // Проверка загруженности и динамическое изменение idleBetweenPolls
        factory.getContainerProperties().setConsumerRebalanceListener(new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // Увеличиваем паузу, если сообщений нет
                idleBetweenPolls.set(5000); // Устанавливаем 5 секунд, когда сообщений нет
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                // Уменьшаем паузу, если начался плотный поток сообщений
                idleBetweenPolls.set(100); // Устанавливаем 100 мс при плотном потоке сообщений
            }
        });

        return factory;
    }

    @KafkaListener(topics = "topic1", groupId = "group_id")
    public void listen(String message) {
        System.out.println("Received Message: " + message);
        // Обработка сообщения
    }
}
```

max.poll.interval.ms - Map<String, Object> props = new HashMap<>();  

props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 900000); // 15 минут

- Описание: Это максимальное время в миллисекундах, которое консюмер может проводить между вызовами метода poll без риска быть признанным неактивным и исключенным из группы консюмеров.
- Назначение: Если консюмер не вызывает метод poll в течение этого времени, брокер Kafka считает его неактивным и перераспределяет его партиции другим активным консюмерам в группе.
- По умолчанию: 300000 мс (5 минут).
- Использование: Настраивается для управления долговременной обработкой сообщений или в ситуациях, когда могут быть долгие периоды без вызова poll.
Если у вас есть только один консюмер в группе и обработка сообщения занимает больше времени, чем значение max.poll.interval.ms,
то консюмер будет считаться неактивным. В этом случае произойдут следующие шаги:
- Консюмер считается неактивным: После истечения времени max.poll.interval.ms, Kafka решит, что консюмер неактивен, потому что он не вызвал poll в течение установленного интервала.
- Проброс исключения: Консюмер получит исключение
org.apache.kafka.clients.consumer.internals.ConsumerCoordinator$OffsetCommitTimeoutException или
org.apache.kafka.clients.consumer.internals.ConsumerCoordinator$CommitFailedException,
которое указывает на то, что консюмер не смог успешно выполнить коммит смещения.
- Закрытие консюмера: Если консюмер не может поддерживать соединение с брокером из-за превышения max.poll.interval.ms, он будет закрыт.
После закрытия консюмера никакие новые сообщения не будут прочитаны до тех пор, пока приложение не перезапустит консюмер.
- Зависимость от настроек auto.offset.reset: Если auto.offset.reset настроен на earliest или latest,
то при следующем запуске консюмера он либо начнет читать с самого начала топика (в случае earliest), либо с конца (в случае latest), в зависимости от того, как настроено ваше приложение.

pollTimeout - время в течение которого Kafka Consumer, после запроса poll, удерживает сетевое соединение с брокером в ожидании новых соединений.
- Spring Kafka Listener Container передает эти сообщения методу, аннотированному @KafkaListener, для обработки.
- Если обработка сообщения занимает больше времени, чем pollTimeout, Kafka Consumer не вызывает poll повторно, пока обработка текущего сообщения не завершится.
- Если сообщений не было получено, то через время idle-between-polls запрос poll повторяется

max.poll.records - Этот параметр управляет количеством записей, которые потребитель может получать за один вызов poll().
Если вы уменьшите его, потребитель сможет быстрее обрабатывать меньшее количество сообщений, что поможет избежать нарушения max.poll.interval.ms.

**CommonProperties:**  
- bootstrap-servers - Адреса брокеров Kafka (например, localhost:9092)
- client.id

ssl:
- ssl.key.password
- ssl.keystore.location
- ssl.keystore.password
- ssl.keystore.type
- ssl.truststore.location
- ssl.truststore.passwor
- ssl.truststore.type
- ssl.protocol

security:
- security.protocol
---
consumer
- auto-commit-interval-ms: Интервал автоматического подтверждения смещений
- auto-offset-reset: Определяет, с какого смещения начинать чтение (например, earliest, latest).
- bootstrap.servers
- client.id
- enable-auto-commit: Включает или отключает автоматическое подтверждение смещений
- fetch.max.wait.ms
- fetch.min.bytes
- group-id: Идентификатор группы консюмеров
- heartbeat.interval.ms
- isolation.level
- key-deserializer: Класс десериализатора для ключей сообщений
- value-deserializer: Класс десериализатора для значений сообщений
- max-poll-records: Максимальное количество записей, которые можно получить за один вызов poll
- max-poll-interval-ms: Максимальное время между вызовами poll.
- session-timeout-ms: Время ожидания для обнаружения неприступных консюмеров
```dockerfile
spring:
  kafka:
    consumer:
      group-id: group_id
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: true
      auto-commit-interval-ms: 1000
      session-timeout-ms: 10000
      max-poll-records: 500
      max-poll-interval-ms: 300000
      properties:
        max-partition-fetch-bytes: 1048576
```
---	
producer:
- acks - Уровень подтверждения записи (например, all, 1, 0)
- batch-size - Размер пакета сообщений для отправки
- bootstrap.servers
- buffer-memory - Размер памяти для буферизации сообщений
- client.id
- compression.type
- key-serializer - Класс сериализатора для ключей сообщений
- retries - Количество попыток повторной отправки сообщений в случае ошибки
- value-serializer - Класс сериализатора для значений сообщений
- linger-ms - Время ожидания перед отправкой пакета

```dockerfile
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 1
      buffer-memory: 33554432
      properties:
        max-request-size: 10485760
```
---	
listener:

- ack-mode: Режим подтверждения обработки сообщений (например, manual, record, batch).
- concurrency: Количество потоков для обработки сообщений.
- missing-topics-fatal: Определяет, будет ли ошибка при отсутствии топиков фатальной.
- container: Конфигурация контейнера слушателя.
- idle-between-polls: Время ожидания между вызовами poll.
- poll-timeout: Таймаут ожидания при вызове poll.
- recovery: Настройки для обработки ошибок и повторных попыток (например, retries).
```dockerfile
spring:
  kafka:
    listener:
      ack-mode: manual
      concurrency: 3
      missing-topics-fatal: false
      container:
        idle-between-polls: 1000
        poll-timeout: 3000
        recovery:
          retries: 3
```
---

![альтернативы Kafka.png](src/main/resources/image/альтернативы%20Kafka.png)

[Apache Kafka Documentation](https://kafka.apache.org/documentation/)  
[Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/index.html)  
[Spring for Apache Kafka 3.2.3 API](https://docs.spring.io/spring-kafka/api/index.html)  
[Про Kafka (основы) - Владимир Богдановский](https://www.youtube.com/watch?v=-AZOi3kP9Js)  
[Spring + Kafka навсегда - плейлист](https://www.youtube.com/playlist?list=PL3YLcFohmEErXLr_Bg1lV0RWUrbmN6dbH)  
[Курс | Apache Kafka от OTUS](https://www.youtube.com/playlist?list=PLfnFOImnyWRX_EvkfNXFB977BCVOS1MXB)  
[Много статей по Kafka на сайте BigDataSchool.ru](https://bigdataschool.ru/blog/news/kafka)  
[Под капотом продюсера Kafka: UML-диаграмма публикации сообщений](https://bigdataschool.ru/blog/kafka-producer-uml-sequence.html)  
[UML-диаграмма последовательности потребления сообщений из Kafka](https://bigdataschool.ru/blog/uml-sequence-for-kafka-consumer.html)  
[Под капотом Apache Kafka: разбираемся с файлами хранения и механизмами обработки данных](https://bigdataschool.ru/blog/kafka-under-the-hood-files-overview.html)  
[Стратегии потребления Spring Kafka и обработка ошибок | ЯКоВ | RU](https://www.youtube.com/watch?v=OnZ7JArSoiU)  
[Диспетчерская на базе Spring и Kafka. Полный курс](https://www.youtube.com/watch?v=wdljVVzhZNc)  

