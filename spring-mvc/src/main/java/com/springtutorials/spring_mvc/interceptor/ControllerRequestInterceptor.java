package com.springtutorials.spring_mvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class ControllerRequestInterceptor implements HandlerInterceptor {
    private static String CRLF = "\n";
    private static String EMPTY = "EMPTY";

    /**
     * Вызывается перед тем, как контроллер обработает запрос. Возвращает true, если запрос должен продолжить обработку.
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        requestLogging(request);
        return true;
    }

    /**
     * Вызывается после обработки запроса, но перед отправкой ответа клиенту
     */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {

    }

    private void requestLogging(HttpServletRequest request) throws IOException {
        log.info("URI запроса: {}", request.getRequestURI());
        log.info("URL запроса: {}", request.getRequestURL());
        log.info("Метод HTTP: {}", request.getMethod());
        log.info("Тело запроса: {}", getRequestBodyViaInputStream(request));
        log.info("Параметры запроса: {}", getRequestParametersString(request));
        log.info("Значение заголовков запроса: {}", getHeaderString(request));
        log.info("IP-адрес клиента: {}", request.getRemoteAddr());
        log.info("Имя хоста клиента: {}", request.getRemoteHost());
        log.info("Порт клиента: {}", request.getRemotePort());
        log.info("Текущая сессия: {}", request.getSession());
        log.info("Уникальный идентификатор текущей сессии: {}", request.getSession().getId());
        log.info("Атрибуты сессии: {}", getSessionAttributes(request));
        log.info("Имя сервера, на котором запущено приложение: {}", request.getServerName());
        log.info("Порт сервера: {}", request.getServerPort());
        log.info("Путь контекста приложения: {}", request.getContextPath());
    }

    private String getRequestParametersString(HttpServletRequest request) {
        StringBuilder parameterBuilder = new StringBuilder();
        request.getParameterMap().forEach((paramName, paramValue) ->
                parameterBuilder.append(capitalize(paramName)).append(": ")
                        .append(request.getParameter(paramName)).append(CRLF)
        );
        return (parameterBuilder.isEmpty()) ? EMPTY : CRLF + parameterBuilder + CRLF;
    }

    private String getRequestBodyViaInputStream(HttpServletRequest request) throws IOException {
        String body = new BufferedReader(
                new InputStreamReader(request.getInputStream(),
                        StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining(CRLF));
        return (body.isEmpty()) ? EMPTY : CRLF + body + CRLF;
    }

    private String getRequestBodyViaReader(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining(CRLF));
        return (body.isEmpty()) ? EMPTY : CRLF + body + CRLF;
    }

    /**
     * Вызывается после завершения всего процесса обработки запроса, включая отображение представления (если оно есть)
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) throws Exception {
    }

    private String getHeaderString(HttpServletRequest request) {
        StringBuilder headerBuilder = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headerBuilder.append(capitalize(headerName)).append(": ")
                    .append(request.getHeader(headerName)).append(CRLF);
        }
        return (headerBuilder.isEmpty()) ? EMPTY : CRLF + headerBuilder + CRLF;
    }

    private String getSessionAttributes(HttpServletRequest request) {
        StringBuilder attributeBuilder = new StringBuilder();
        Enumeration<String> attributeNames = request.getSession().getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            attributeBuilder.append(capitalize(attributeName)).append(": ")
                    .append(request.getSession().getAttribute(attributeName)).append(CRLF);
        }

        return (attributeBuilder.isEmpty()) ? EMPTY : CRLF + attributeBuilder + CRLF;
    }

    private String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
