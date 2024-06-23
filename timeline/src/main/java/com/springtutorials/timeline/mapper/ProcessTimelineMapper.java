package com.springtutorials.timeline.mapper;

import com.springtutorials.timeline.common.dto.process.ProcessStepInfoDto;
import com.springtutorials.timeline.common.model.process.ProcessStepInfo;
import com.springtutorials.timeline.common.model.process.ProcessTimeline;
import com.springtutorials.timeline.common.model.process.ProcessTimelineStep;
import com.springtutorials.timeline.dto.rest.process.ProcessStepDto;
import com.springtutorials.timeline.dto.rest.process.ProcessTimelineDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProcessTimelineMapper {

    ProcessStepInfoDto toStepInfoDto(ProcessStepInfo processStepInfo);

    @Mapping(target = "stepDefinitionCode", expression = "java(processTimelineStep.getStepDefinition().getCode())")
    @Mapping(target = "description", expression = "java(processTimelineStep.getStepDefinition().name())")
    @Mapping(source = "processInfo", target = "processStepInfo")
    ProcessStepDto toStepDto(ProcessTimelineStep processTimelineStep);

    ProcessTimelineDto toDto(ProcessTimeline processTimeline);
}

