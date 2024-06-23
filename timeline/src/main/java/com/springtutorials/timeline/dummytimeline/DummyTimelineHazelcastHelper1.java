package com.springtutorials.timeline.dummytimeline;

import com.hazelcast.core.HazelcastInstance;
import com.springtutorials.timeline.common.service.hazelcast.AbstractHazelcastProcessingHelper;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service("dummyTimelineHazelcastHelper1")
public class DummyTimelineHazelcastHelper1 extends AbstractHazelcastProcessingHelper<String> {

    protected DummyTimelineHazelcastHelper1(HazelcastInstance hazelcastInstance) {
        super(hazelcastInstance);
    }

    @Override
    protected String getMapName() {
        return "DUMMY_TIMELINE";
    }

    @Override
    protected String getLockName() {
        return "DUMMY_TIMELINE_LOCK";
    }

    @Override
    protected Function<String, String> getIdExtractor() {
        return Function.identity();
    }
}
