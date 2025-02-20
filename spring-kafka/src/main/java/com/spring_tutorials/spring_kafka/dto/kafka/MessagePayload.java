package com.spring_tutorials.spring_kafka.dto.kafka;

import lombok.Data;

@Data
public class MessagePayload {
    private String fileId;
    private Integer sleep = 0;
}
