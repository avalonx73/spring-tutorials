package com.springtutorials.spring_async.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CustomTaskExecutionHandlerImpl implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            executor.getQueue().put(r);
        }
        catch (InterruptedException e) {
            log.error("error", e);
            throw new RejectedExecutionException(e.getMessage(), e);
        }
    }
}
