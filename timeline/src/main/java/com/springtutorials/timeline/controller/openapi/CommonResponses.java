package com.springtutorials.timeline.controller.openapi;

import com.springtutorials.timeline.common.controller.ResponseWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonResponses {

    public static class StringResponse extends ResponseWrapper<String> {}
}
