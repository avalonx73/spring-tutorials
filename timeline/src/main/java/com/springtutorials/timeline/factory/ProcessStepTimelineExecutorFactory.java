package com.springtutorials.timeline.factory;

import com.springtutorials.timeline.common.exception.ProcessTimelineStepException;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStepDefinition;
import com.springtutorials.timeline.service.process.ProcessTimelineStepExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessStepTimelineExecutorFactory {
    private static final Map<ProcessTimelineStepDefinition, ProcessTimelineStepExecutorService> STEP_EXECUTORS =
            new EnumMap<>(ProcessTimelineStepDefinition.class);

    @Autowired
    private ProcessStepTimelineExecutorFactory(List<ProcessTimelineStepExecutorService> executorServices) {
        for (ProcessTimelineStepExecutorService executorService : executorServices) {
            STEP_EXECUTORS.put(executorService.getStepDefinition(), executorService);
        }
    }

    public static ProcessTimelineStepExecutorService getStepExecutorService(ProcessTimelineStepDefinition type) {
        ProcessTimelineStepExecutorService stepExecutorService = STEP_EXECUTORS.get(type);
        if (stepExecutorService == null) {
            throw new ProcessTimelineStepException("Unknown service type: " + type);
        }
        return stepExecutorService;
    }
}

