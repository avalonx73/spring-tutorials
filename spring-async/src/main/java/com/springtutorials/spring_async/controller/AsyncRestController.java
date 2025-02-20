package com.springtutorials.spring_async.controller;

import com.springtutorials.spring_async.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AsyncRestController {
    private final AsyncService asyncService;

    @GetMapping("/startup1")
    public ResponseEntity<String> startup1(@RequestParam Integer count) throws InterruptedException {
        asyncService.process(count);

        /**
         * HTTP 202 Accepted означает, что запрос был принят на обработку, но обработка еще не завершена и
         * клиент может продолжать работу, не дожидаясь завершения.
         * Идеально подходит для асинхронных операций
         *
         * Когда использовать HTTP 202 Accepted:
         * 1. Длительные операции
         * 2. Асинхронная обработка
         * 3. Фоновые задачи
         * 4. Операции, требующие времени на выполнение
         */
        return ResponseEntity.accepted().body("Processing started");
    }

    @GetMapping("/startup2")
    public ResponseEntity<String> startup2(@RequestParam Integer count) {

        CompletableFuture.runAsync(()->{
            List<String> results = asyncService.processAllItemsWithProgress(count);
            log.info("Processing completed, total results: {}", results.size());
        });

        return ResponseEntity.accepted().body("Processing started");
    }

    @GetMapping("/startup3")
    public ResponseEntity<String> startup3(@RequestParam Integer count) {

        CompletableFuture.runAsync(()->{
            List<String> results = asyncService.processAllRequests(count);
            log.info("Processing completed, total results: {}", results.size());
        });

        return ResponseEntity.accepted().body("Processing started");
    }

    @GetMapping("/process")
    public ResponseEntity<String> externalServiceMock(@RequestParam String id) throws InterruptedException {
        Thread.currentThread().setName("rest_" + id);
        log.info("id={} Process start", id);
        Thread.sleep(20000);
        log.info("id={} Process end", id);
        return ResponseEntity.ok(id);
    }

}
