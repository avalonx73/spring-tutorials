В Spring `ThreadPoolTaskExecutor` — это реализация пула потоков, основанная на классе `ThreadPoolExecutor` из пакета `java.util.concurrent`. Он управляет созданием потоков, их завершением и очередью задач. Основные параметры конфигурации включают: `corePoolSize`, `maxPoolSize`, `queueCapacity`, и они работают вместе для контроля выполнения потоков и задач.

## 1. **corePoolSize**

`corePoolSize` — это минимальное количество потоков, которые будут постоянно активны в пуле, даже если они не заняты выполнением задач.

### Основные моменты:
- Если число активных потоков меньше значения `corePoolSize`, пул создаст новые потоки для обработки задач.
- Даже если задачи завершены, пул не уничтожает потоки, если их количество не превышает `corePoolSize`.
- Если задачи отправляются в пул, а все потоки из `corePoolSize` заняты, задачи добавляются в очередь (если очередь не переполнена).

### Пример:

```java
executor.setCorePoolSize(5);
```

- Это означает, что пул всегда будет держать 5 потоков, даже если нет задач для выполнения.

## 2. **maxPoolSize**

`maxPoolSize` — это максимальное количество потоков, которые может создать пул потоков. Это верхний предел числа одновременных потоков.
### Основные моменты:
- Если очередь заполнена и все потоки из `corePoolSize` заняты, пул начнет создавать новые потоки до значения `maxPoolSize`.
- После завершения задач, количество потоков, превышающее `corePoolSize`, будет уничтожено (в зависимости от параметра `keepAliveTime`).
- Максимальный размер пула полезен для обеспечения пропускной способности, чтобы справиться с неожиданными скачками нагрузки, или когда вы хотите контролировать верхний предел создания потоков.

### Взаимодействие с `corePoolSize`:
- Если число активных потоков достигло `corePoolSize`, задачи помещаются в очередь.
- Когда очередь заполнена, пул начнет создавать дополнительные потоки до значения `maxPoolSize`.
- Как только количество потоков превысит `maxPoolSize`, задачи будут отклонены (или обработаны согласно стратегии отклонения `RejectedExecutionHandler`).

### Пример:

```java
executor.setMaxPoolSize(10);
```

- Это означает, что пул может создать максимум 10 потоков для обработки задач.

## 3. **queueCapacity**

`queueCapacity` — это размер очереди задач, которые могут быть поставлены на ожидание выполнения, если все потоки заняты.

### Основные моменты:
- Если все потоки из `corePoolSize` заняты, задачи отправляются в очередь.
- Если очередь переполнена и потоков меньше, чем `maxPoolSize`, создаются новые потоки для обработки задач.
- Если очередь заполнена и количество потоков достигло `maxPoolSize`, задачи будут отклонены (или обработаны согласно стратегии отклонения).

### Взаимодействие с `corePoolSize` и `maxPoolSize`:
- Если число активных потоков меньше или равно `corePoolSize`, задачи помещаются в очередь, пока она не заполнится.
- Когда очередь заполнена и количество потоков меньше `maxPoolSize`, создаются новые потоки для выполнения задач.
- Если очередь переполнена и количество потоков достигло `maxPoolSize`, задачи будут отклонены.

### Пример:

```java
executor.setQueueCapacity(50);
```

- Это означает, что пул может поставить в очередь до 50 задач, если все потоки заняты.

## Взаимосвязь параметров

1. **Создание потоков:**
    - Если число активных потоков меньше, чем `corePoolSize`, пул создаст новый поток для выполнения задачи.
    - Если число активных потоков равно `corePoolSize`, задачи будут помещаться в очередь.
    - Когда очередь заполнена, пул начнет создавать новые потоки до значения `maxPoolSize`.

2. **Очередь задач:**
    - Если все потоки заняты и очередь задач не заполнена, задачи будут помещены в очередь (до значения `queueCapacity`).
    - Если очередь задач заполнена, и пул не достиг `maxPoolSize`, пул начнет создавать дополнительные потоки для выполнения задач.

3. **Отклонение задач:**
    - Если количество потоков достигло `maxPoolSize` и очередь заполнена, задачи будут отклонены (или обработаны согласно выбранной политике, например, `CallerRunsPolicy`, `AbortPolicy` и т.д.).

## Пример конфигурации

```java
@Bean
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(25);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // Политика при отказе
    executor.initialize();
    return executor;
}
```

- В этой конфигурации пул будет содержать 5 потоков постоянно (`corePoolSize = 5`).
- Если все 5 потоков заняты, задачи будут помещены в очередь (до 25 задач в очереди).
- Если очередь заполнена, пул начнет создавать новые потоки до максимума 10 потоков.
- Если и очередь заполнена, и количество потоков достигло 10, задачи будут выполнены в текущем потоке (благодаря `CallerRunsPolicy`).



В Java пул потоков с помощью классов из пакета `java.util.concurrent` предоставляет различные механизмы для обработки ситуации, когда пул потоков перегружен и не может принять новые задачи. В таких случаях может выбрасываться исключение или применяться определенные стратегии. Эти стратегии и исключения управляются с помощью следующих классов и политик: `CallerRunsPolicy`, `AbortPolicy`, `DiscardPolicy`, `DiscardOldestPolicy`, а также исключения `TaskRejectedException`.

## 1. **CallerRunsPolicy**

`CallerRunsPolicy` — это одна из стратегий поведения, которая срабатывает, когда пул потоков не может принять новую задачу, потому что очередь заполнена или пул достиг максимального размера потоков.

### Поведение:
- Если пул потоков не может выполнить задачу, **задача будет выполнена в текущем потоке**, то есть в потоке, который вызвал метод отправки задачи (например, `execute` или `submit`).
- Эта политика помогает уменьшить нагрузку на пул потоков, так как текущий поток займется выполнением задачи, но может привести к задержкам в выполнении последующих задач, так как текущий поток будет занят.

### Пример:

```java
ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<>(2), new ThreadPoolExecutor.CallerRunsPolicy());
```

- Здесь, если очередь и пул заполнены, текущий поток выполнит задачу.

## 2. **AbortPolicy**

`AbortPolicy` — это стандартная стратегия, используемая по умолчанию в `ThreadPoolExecutor`. Она выбрасывает исключение, если новая задача не может быть добавлена в очередь или пул потоков.

### Поведение:
- Если пул или очередь заполнены, вызов метода отправки задачи вызовет исключение `RejectedExecutionException`.
- Это поведение помогает быстро обнаружить, что система перегружена и задачи не могут быть обработаны, но оно не пытается решить проблему.

### Пример:

```java
ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<>(2), new ThreadPoolExecutor.AbortPolicy());
```

- Если пул и очередь заполнены, будет выброшено исключение `RejectedExecutionException`.

## 3. **DiscardPolicy**

`DiscardPolicy` — это стратегия, при которой задача просто **отбрасывается** без каких-либо уведомлений, если пул не может ее обработать.

### Поведение:
- Если пул и очередь заполнены, задача будет отброшена, и никаких исключений не будет выброшено.
- Этот подход может привести к потере задач, если система перегружена, но не будет выбрасывать исключения или блокировать выполнение.

### Пример:

```java
ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<>(2), new ThreadPoolExecutor.DiscardPolicy());
```

- Если пул и очередь заполнены, задачи будут просто отброшены.

## 4. **DiscardOldestPolicy**

`DiscardOldestPolicy` — это стратегия, при которой, если пул и очередь заполнены, **отбрасывается самая старая задача** из очереди, чтобы освободить место для новой задачи.

### Поведение:
- Если пул потоков и очередь заполнены, самая старая задача в очереди будет удалена, и новая задача будет добавлена в очередь.
- Это может быть полезно, если приоритет новых задач выше, чем выполнение старых.

### Пример:

```java
ExecutorService executor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<>(2), new ThreadPoolExecutor.DiscardOldestPolicy());
```

- Если очередь заполнена, самая старая задача будет удалена, чтобы принять новую.

## 5. **TaskRejectedException**

`TaskRejectedException` — это исключение, которое выбрасывается, если задача отклонена пулом потоков в `ThreadPoolTaskExecutor`. По сути, это производное от стандартного исключения `RejectedExecutionException`.

### Когда оно выбрасывается:
- Это исключение может выбрасываться в ситуациях, когда пул потоков не может принять новую задачу, и используется политика обработки, которая приводит к отказу выполнения задачи (например, `AbortPolicy`).
- Это исключение сигнализирует о том, что задача не была принята в пул для обработки.

### Использование в Spring:
- В Spring классе `ThreadPoolTaskExecutor`, это исключение может возникать, если пул потоков перегружен, и активна политика, которая не принимает новые задачи.

### Пример настройки Spring `ThreadPoolTaskExecutor` с политикой `AbortPolicy`:

```java
@Bean
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(25);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // Выбрасывает исключение
    executor.initialize();
    return executor;
}
```

## Заключение:
- **CallerRunsPolicy**: Если очередь заполнена, задача выполняется в текущем потоке.
- **AbortPolicy**: Выбрасывает исключение, если задача не может быть добавлена.
- **DiscardPolicy**: Просто отбрасывает задачу без уведомлений.
- **DiscardOldestPolicy**: Отбрасывает самую старую задачу в очереди, чтобы принять новую.
- **TaskRejectedException**: Исключение, которое сигнализирует, что задача была отклонена пулом потоков.

Эти политики позволяют гибко управлять поведением пула потоков при перегрузке, и выбор подходящей стратегии зависит от требований к приложению.


[Guide to RejectedExecutionHandler](https://www.baeldung.com/java-rejectedexecutionhandler)  
[TaskRejectedException in ThreadPoolTaskExecutor](https://stackoverflow.com/questions/49290054/taskrejectedexception-in-threadpooltaskexecutor)  
[Problems and Solutions when using Async in Spring Boot](https://lastjavabuilder.medium.com/problems-and-solutions-when-using-async-in-spring-boot-e383f9d3b45d)  
[Rules of a ThreadPoolExecutor pool size](https://www.bigsoft.co.uk/blog/2009/11/27/rules-of-a-threadpoolexecutor-pool-size)  
[Class ThreadPoolExecutor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadPoolExecutor.html)  
[ThreadPoolTaskExecutor corePoolSize vs. maxPoolSize](https://www.baeldung.com/java-threadpooltaskexecutor-core-vs-max-poolsize)  
[What are `corePoolSize` and `maxPoolSize` in thread pool configuration? When is `maxPoolSize` used?](https://medium.com/@raksmeykoung_19675/what-are-corepoolsize-and-maxpoolsize-in-thread-pool-configuration-when-is-maxpoolsize-used-65a84258fea6)  
