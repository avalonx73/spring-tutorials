package com.springtutorials.spring_async.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {
    private final SenderService senderService;

    @Async("controllerExecutor")
    public void process(int count) throws InterruptedException {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            log.info("id={} iteration", i);
            Thread.currentThread().setName("process_" + i);
            CompletableFuture<Void> future = senderService.send(i);
            futures.add(future);
        }
        log.info("FINISH ITERATE");
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join(); // Блокируем поток до завершения всех задач
        log.info("FINISH TASKS");
    }
}
