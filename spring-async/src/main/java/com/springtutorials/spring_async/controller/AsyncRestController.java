package com.springtutorials.spring_async.controller;

import com.springtutorials.spring_async.service.AsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AsyncRestController {
    private final AsyncService asyncService;

    @GetMapping("/startup")
    public String startup(@RequestParam Integer count) throws InterruptedException {
        asyncService.process(count);
        return "OK";
    }

    @GetMapping("/process")
    public String sender(@RequestParam String id) throws InterruptedException {
        Thread.currentThread().setName("rest_" + id);
        log.info("id={} Process start", id);
        Thread.sleep(60000);
        log.info("id={} Process end", id);
        return "OK";
    }

}
