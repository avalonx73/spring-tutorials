package com.springtutorials.timeline.controller.v1.client;

import com.springtutorials.timeline.common.controller.RequestId;
import com.springtutorials.timeline.common.controller.ResponseWrapper;
import com.springtutorials.timeline.common.dto.ProcessTimelineStepMetadataDto;
import com.springtutorials.timeline.controller.CommonApiConstants;
import com.springtutorials.timeline.dto.rest.process.FinishStepDto;
import com.springtutorials.timeline.dto.rest.process.RollbackStepDto;
import com.springtutorials.timeline.service.process.ProcessTimelineStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.springtutorials.timeline.common.util.ApiUtils.wrapPayload;

@RestController
@RequestMapping(CommonApiConstants.VERSION_1)
@RequiredArgsConstructor
public class ProcessTimelineStepController {

    private static final String PAYMENT_TIMELINE_STEP_PATH = "/timelines/steps";

    private final RequestId requestId;
    private final ProcessTimelineStepService processTimelineStepService;

    @PatchMapping(PAYMENT_TIMELINE_STEP_PATH + "/start")
    public ResponseEntity<ResponseWrapper<String>> startTimelineStep(
           @RequestBody ProcessTimelineStepMetadataDto stepMetadataDto) {
        processTimelineStepService.startStep(stepMetadataDto);
        return ResponseEntity.ok(wrapPayload("STARTED", requestId));
    }

    @PatchMapping(PAYMENT_TIMELINE_STEP_PATH + "/finish")
    public ResponseEntity<ResponseWrapper<String>> finishTimelineStep(
           @RequestBody FinishStepDto finishStepDto) {
        processTimelineStepService.finishStep(finishStepDto);
        return ResponseEntity.ok(wrapPayload("FINISHED", requestId));
    }

    @PatchMapping(PAYMENT_TIMELINE_STEP_PATH + "/rollback")
    public ResponseEntity<ResponseWrapper<String>> rollbackTimelineStep(
            @RequestBody RollbackStepDto rollbackStepDto) {
        processTimelineStepService.rollBackStep(rollbackStepDto);
        return ResponseEntity.ok(wrapPayload("Rolled back", requestId));
    }
}

