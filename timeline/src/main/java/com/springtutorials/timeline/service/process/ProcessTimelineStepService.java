package com.springtutorials.timeline.service.process;

import com.springtutorials.timeline.common.dto.ProcessTimelineStepMetadataDto;
import com.springtutorials.timeline.dto.rest.process.FinishStepDto;
import com.springtutorials.timeline.dto.rest.process.RollbackStepDto;

public interface ProcessTimelineStepService {

    void startStep(ProcessTimelineStepMetadataDto stepMetadataDto);

    void finishStep(FinishStepDto finishStepDto);

    void updateStepMessage(ProcessTimelineStepMetadataDto stepMetadataDto);

    void updateStepStatus(ProcessTimelineStepMetadataDto stepMetadataDto);

    void rollBackStep(RollbackStepDto rollbackStepDto);

}