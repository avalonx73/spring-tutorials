package com.springtutorials.spring_async.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {
    private final SenderService senderService;
    private final ThreadPoolTaskExecutor executor;

    @Async("controllerExecutor")
    public void process(int count) throws InterruptedException {
        List<CompletableFuture<String>> futures = new ArrayList<>();

        // цикл выполняется очень быстро - он только создаёт и запускает задачи, но не ждёт их завершения
        for (int i = 1; i <= count; i++) {
            log.info("id={} iteration", i);
            Thread.currentThread().setName("process_" + i);
            CompletableFuture<String> future = senderService.sendAsync(i);
            futures.add(future);
        }

        log.info("FINISH ITERATE");
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Блокируем поток и ждем завершения всех задач
        allFutures.join();

        log.info("FINISH TASKS");
    }

    /**
     * Метод с обработкой ошибок и прогрессом
     */
    public List<String> processAllItemsWithProgress(Integer totalItems) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        AtomicInteger completed = new AtomicInteger(0);

        for (int i = 0; i < totalItems; i++) {
            CompletableFuture<String> future = processItem(i)
                    .thenApply(result -> {
                        int count = completed.incrementAndGet();
                        if (count % 1000 == 0) {
                            log.info("Processed {}/{} items", count, totalItems);
                        }
                        return result;
                    })
                    .exceptionally(throwable -> {
                        log.error("Error processing item: {}", throwable.getMessage());
                        return "Error processing item";
                    });
            futures.add(future);
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CompletableFuture<String> processItem(int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Имитация длительной обработки
                Thread.sleep(1000);
                return "Processed item " + itemId;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException(e);
            }
        }, executor);
    }

    public List<String> processAllRequests(Integer totalRequests) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        // Создаем группы по 100 запросов для контроля нагрузки
        int batchSize = 100;
        for (int batch = 0; batch < totalRequests; batch += batchSize) {
            int endIndex = Math.min(batch + batchSize, totalRequests);

            for (int i = batch; i < endIndex; i++) {
                final int requestId = i;
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return senderService.callExternalService(requestId)
                                // Действие при успешном выполнении
                                .doOnSuccess(result -> {
                                    int count = completed.incrementAndGet();
                                    if (count % 100 == 0) {
                                        log.info("Completed {}/{} requests, {} failed",
                                                count, totalRequests, failed.get());
                                    }
                                })
                                // Действие при ошибке
                                .doOnError(error -> failed.incrementAndGet())
                                // Блокирующее ожидание результата
                                .block(Duration.ofMinutes(3));
                    } catch (Exception e) {
                        // Обработка ошибок таймаута и других исключений
                        failed.incrementAndGet();
                        log.error("Critical error for request {}: {}", requestId, e.getMessage());
                        return "Critical error for request " + requestId;
                    }
                }, executor);

                futures.add(future);
            }

            // Небольшая пауза между группами запросов
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }
        }

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get(3, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        log.error("Error getting result: {}", e.getMessage());
                        return "Error: " + e.getMessage();
                    }
                })
                .collect(Collectors.toList());
    }
}

