package com.springtutorials.spring_async.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderService {
    private final String EXTERNAL_SERVICE_URL = "http://localhost:8080/process";
    private final RestTemplate restTemplate;
    private final ThreadPoolTaskExecutor executor;
    private final WebClient webClient;


    /**
     * <li>getForObject() - Выполняет HTTP GET-запрос и возвращает тело ответа в виде объекта
     * <li>getForEntity() - Выполняет HTTP GET-запрос и возвращает объект ResponseEntity, который содержит как тело ответа, так и метаинформацию (например, заголовки, статус).
     * <li>postForObject() - Выполняет HTTP POST-запрос с телом запроса и возвращает тело ответа в виде
     * <li>postForEntity() - Выполняет HTTP POST-запрос и возвращает объект ResponseEntity
     * <li>patchForObject()
     * <p>
     * <li>put() - Выполняет HTTP PUT-запрос для замены ресурса без возвращения ответа.
     * <li>delete() - Выполняет HTTP DELETE-запрос для удаления ресурса.
     * <li>exchange() - Это универсальный метод, который можно использовать для выполнения любых HTTP-запросов с указанием метода (GET, POST, PUT, DELETE, и т.д.), заголовков, тела запроса и класса ответа.
     * <li>execute() - Этот метод предоставляет возможность полной настройки запроса. Можно определить процесс формирования запроса и обработки ответа.
     */
    public String send(int id) throws InterruptedException {
        log.info("id={} Request", id);
        String url = EXTERNAL_SERVICE_URL + "?id={id}";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class,
                id);

        log.info("id={} Response", response);
        return response.toString();
    }

    /**
     * <li>Spring создаёт прокси
     * <li>Запускает реальное выполнение метода в отдельном потоке из пула
     * <li>Немедленно возвращает пустой CompletableFuture
     * <li>CompletableFuture находится в состоянии "не завершён" пока выполняется метод (incomplete)
     * <li>Когда метод завершиться, Future перейдёт в состояние "завершён" (completed)
     * <li>AsyncAnnotationBeanPostProcessor обрабатывает аннотацию @Async
     */
    @Async("senderExecutor")
    public CompletableFuture<String> sendAsync(int id) throws InterruptedException {
        send(id);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Примерно так выглядит прокси, который Spring создает для метода sendAsync
     */
    public CompletableFuture<String> sendProxy(int id) {
        // Создаем future для конкретного вызова
        CompletableFuture<String> future = new CompletableFuture<>();

        // Создаем Runnable, который замкнут (closures) на этот конкретный future
        Runnable task = () -> {
            try {
                // Вызываем реальный метод
                String result = send(id);

                // Заполняем именно тот future, который был создан для этого вызова
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        // Запускаем задачу в пуле потоков
        executor.submit(task);

        // Возвращаем future, связанный с этой конкретной задачей
        return future;
    }

    public Mono<String> callExternalService(int requestId) {
        return webClient.get() // Создаем GET запрос
                // .uri(EXTERNAL_SERVICE_URL + "?id=" + requestId)// Указываем URL с параметром
                .uri(builder -> builder
                        .path(EXTERNAL_SERVICE_URL)
                        .queryParam("id", requestId)
                        .build())
                .retrieve()// Выполняет запрос. Возвращает ResponseSpec для обработки ответа
                .bodyToMono(String.class) // Преобразуем тело ответа в Mono<String>. Mono - это контейнер для 0 или 1 значения в реактивном стиле
                // Обрабатываем ошибки
                .doOnError(error -> log.error("Error calling external service for id {}: {}",
                        requestId, error.getMessage()))
                // Настраиваем политику повторных попыток
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5)))
                // Таймаут для запроса
                .timeout(Duration.ofSeconds(10));
    }
}
