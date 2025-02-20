# Полное описание публичных методов CompletableFuture

## Статические методы создания

| Метод | Описание |
|-------|-----------|
| `supplyAsync(Supplier<U>)` | Создает CompletableFuture, который выполняет задачу асинхронно и возвращает результат |
| `supplyAsync(Supplier<U>, Executor)` | То же, что и выше, но с указанным исполнителем |
| `runAsync(Runnable)` | Создает CompletableFuture для асинхронного выполнения задачи без возврата результата |
| `runAsync(Runnable, Executor)` | То же, что и выше, но с указанным исполнителем |
| `completedFuture(U)` | Создает уже завершенный CompletableFuture с указанным результатом |

## Методы преобразования

| Метод | Описание |
|-------|-----------|
| `thenApply(Function)` | Преобразует результат синхронно |
| `thenApplyAsync(Function)` | Преобразует результат асинхронно |
| `thenApplyAsync(Function, Executor)` | Преобразует результат асинхронно с указанным исполнителем |
| `thenAccept(Consumer)` | Обрабатывает результат без возврата значения синхронно |
| `thenAcceptAsync(Consumer)` | Обрабатывает результат без возврата значения асинхронно |
| `thenRun(Runnable)` | Выполняет действие после завершения синхронно |
| `thenRunAsync(Runnable)` | Выполняет действие после завершения асинхронно |

## Методы композиции

| Метод | Описание |
|-------|-----------|
| `thenCompose(Function)` | Объединяет два CompletableFuture последовательно |
| `thenComposeAsync(Function)` | То же, что и выше, но асинхронно |
| `thenCombine(CompletionStage, BiFunction)` | Объединяет результаты двух CompletableFuture |
| `thenCombineAsync(CompletionStage, BiFunction)` | То же, что и выше, но асинхронно |
| `thenAcceptBoth(CompletionStage, BiConsumer)` | Обрабатывает результаты двух CompletableFuture без возврата значения |
| `thenAcceptBothAsync(CompletionStage, BiConsumer)` | То же, что и выше, но асинхронно |

## Методы обработки нескольких задач

| Метод | Описание |
|-------|-----------|
| `allOf(CompletableFuture...)` | Ожидает завершения всех указанных CompletableFuture |
| `anyOf(CompletableFuture...)` | Ожидает завершения любого из указанных CompletableFuture |

## Методы обработки ошибок

| Метод | Описание |
|-------|-----------|
| `exceptionally(Function)` | Обрабатывает исключения и возвращает резервное значение |
| `handle(BiFunction)` | Обрабатывает как успешное завершение, так и ошибки |
| `handleAsync(BiFunction)` | То же, что и выше, но асинхронно |
| `whenComplete(BiConsumer)` | Выполняет действие после завершения (успех или ошибка) |
| `whenCompleteAsync(BiConsumer)` | То же, что и выше, но асинхронно |

## Методы получения результата

| Метод | Описание |
|-------|-----------|
| `get()` | Блокирует поток и ждет результата |
| `get(long, TimeUnit)` | Блокирует поток и ждет результата с таймаутом |
| `join()` | Блокирует поток и ждет результата без проверяемых исключений |
| `getNow(T)` | Возвращает результат если он готов, иначе возвращает указанное значение |

## Методы проверки состояния

| Метод | Описание |
|-------|-----------|
| `isDone()` | Проверяет, завершилось ли выполнение |
| `isCancelled()` | Проверяет, была ли задача отменена |
| `isCompletedExceptionally()` | Проверяет, завершилось ли выполнение с ошибкой |

## Методы управления выполнением

| Метод | Описание |
|-------|-----------|
| `complete(T)` | Принудительно устанавливает результат |
| `completeExceptionally(Throwable)` | Принудительно устанавливает ошибку |
| `cancel(boolean)` | Отменяет выполнение задачи |
| `obtrudeValue(T)` | Принудительно заменяет результат |
| `obtrudeException(Throwable)` | Принудительно заменяет ошибку |

## Дополнительные методы

| Метод | Описание |
|-------|-----------|
| `toCompletableFuture()` | Преобразует CompletionStage в CompletableFuture |
| `copy()` | Создает новый независимый CompletableFuture с тем же результатом |
| `minimalCompletionStage()` | Создает минимальную версию CompletionStage |
| `completedStage(U)` | Создает завершенный CompletionStage с указанным результатом |
| `failedFuture(Throwable)` | Создает CompletableFuture, завершенный с ошибкой |
| `failedStage(Throwable)` | Создает CompletionStage, завершенный с ошибкой |

## Методы Or-композиции

| Метод | Описание |
|-------|-----------|
| `applyToEither(CompletionStage, Function)` | Применяет функцию к результату того, кто завершится первым |
| `applyToEitherAsync(CompletionStage, Function)` | То же, что и выше, но асинхронно |
| `acceptEither(CompletionStage, Consumer)` | Обрабатывает результат того, кто завершится первым |
| `acceptEitherAsync(CompletionStage, Consumer)` | То же, что и выше, но асинхронно |
| `runAfterEither(CompletionStage, Runnable)` | Выполняет действие после завершения любого из двух |
| `runAfterEitherAsync(CompletionStage, Runnable)` | То же, что и выше, но асинхронно |

## Методы Both-композиции

| Метод | Описание |
|-------|-----------|
| `runAfterBoth(CompletionStage, Runnable)` | Выполняет действие после завершения обоих |
| `runAfterBothAsync(CompletionStage, Runnable)` | То же, что и выше, но асинхронно |