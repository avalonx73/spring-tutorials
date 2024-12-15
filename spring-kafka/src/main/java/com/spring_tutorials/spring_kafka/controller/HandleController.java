package com.spring_tutorials.spring_kafka.controller;

import com.spring_tutorials.spring_kafka.dto.kafka.MessagePayload;
import com.spring_tutorials.spring_kafka.producer.TestKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HandleController {
    private final TestKafkaProducer testKafkaProducer;

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody MessagePayload message) {
        testKafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Message sent");
    }
}
