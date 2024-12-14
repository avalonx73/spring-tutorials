package com.springtutorials.spring_async.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderService {
    private final RestTemplate restTemplate;

    /**
     * getForObject() - Выполняет HTTP GET-запрос и возвращает тело ответа в виде объекта
     * getForEntity() - Выполняет HTTP GET-запрос и возвращает объект ResponseEntity, который содержит как тело ответа, так и метаинформацию (например, заголовки, статус).
     * postForObject() - Выполняет HTTP POST-запрос с телом запроса и возвращает тело ответа в виде
     * postForEntity() - Выполняет HTTP POST-запрос и возвращает объект ResponseEntity
     * patchForObject()
     *
     * put() - Выполняет HTTP PUT-запрос для замены ресурса без возвращения ответа.
     * delete() - Выполняет HTTP DELETE-запрос для удаления ресурса.
     * exchange() - Это универсальный метод, который можно использовать для выполнения любых HTTP-запросов с указанием метода (GET, POST, PUT, DELETE, и т.д.), заголовков, тела запроса и класса ответа.
     * execute() - Этот метод предоставляет возможность полной настройки запроса. Можно определить процесс формирования запроса и обработки ответа.
     *
     */
    @Async("senderExecutor")
    public CompletableFuture<Void> send(int id) throws InterruptedException {
        log.info("id={} Request", id);
        String url = "http://localhost:8080/process?id={id}";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class,
                id);

        log.info("id={} Response", id);
        return CompletableFuture.completedFuture(null);
    }
}
