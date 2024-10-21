package com.springtutorials.spring_client_kafka;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.List;

import static com.springtutorials.spring_client_kafka.utils.Utils.sleepSeconds;

@Slf4j
public class StopWatchTest {
    @Test
    void test() {
        StopWatch stopWatch1 = new StopWatch();
        StopWatch stopWatch2 = new StopWatch();

        stopWatch1.start("Main");
        sleepSeconds(1);
        for (int i = 0; i < 2; i++) {
            stopWatch2.start("Nested-1");
            sleepSeconds(1);
            stopWatch2.stop();
            stopWatch2.start("Nested-2");
            sleepSeconds(1);
            stopWatch2.stop();
        }
        sleepSeconds(1);
        stopWatch1.stop();
        log.info("stopWatch1 = {}", stopWatch1.shortSummary());

        long taskInfo1 = getTaskInfo(stopWatch2, "Nested-1");
        long taskInfo2 = getTaskInfo(stopWatch2, "Nested-1");

        log.info("{} - {}", stopWatch2.getLastTaskInfo().getTimeMillis(), stopWatch2.getTotalTimeMillis());
    }

    private long getTaskInfo(StopWatch stopWatch, String taskName) {
        StopWatch.TaskInfo[] taskInfo = stopWatch.getTaskInfo();
        List<StopWatch.TaskInfo> taskInfo1 = List.of(taskInfo);
        long duration = taskInfo1.stream()
                .filter(t -> taskName.equals(t.getTaskName()))
                .mapToLong(StopWatch.TaskInfo::getTimeMillis).sum();
        return duration;
    }
}
