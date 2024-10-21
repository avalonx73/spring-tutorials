package com.springtutorials.spring_client_kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import static com.springtutorials.spring_client_kafka.utils.Utils.sleepMiliseconds;
import static com.springtutorials.spring_client_kafka.utils.Utils.sleepSeconds;

@Slf4j
public class ApacheStopWatchTest {

    @Test
    void test() {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        sleepMiliseconds(100);
        stopWatch.split();
        log.info("stopWatch1 = {}",stopWatch.toSplitString());

        sleepMiliseconds(150);
        stopWatch.split();
        log.info("stopWatch2 = {}",stopWatch.toSplitString());

        sleepSeconds(1);
        log.info("stopWatch3 = {}",stopWatch);



        sleepSeconds(1);
        stopWatch.split();
        log.info("stopWatch1_ = {}",stopWatch.toSplitString());


        sleepSeconds(1);
        stopWatch.split();
        log.info("stopWatch2_ = {}",stopWatch.toSplitString());


        sleepSeconds(1);
        stopWatch.unsplit();
        log.info("stopWatch4 = {}",stopWatch);


        sleepSeconds(1);
        stopWatch.stop();
        log.info("stopWatch5 = {}",stopWatch);
        sleepSeconds(1);
        log.info("stopWatch6 = {}",stopWatch);


    }
}
