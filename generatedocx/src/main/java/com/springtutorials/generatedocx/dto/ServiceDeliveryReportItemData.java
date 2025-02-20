package com.springtutorials.generatedocx.dto;

import com.springtutorials.generatedocx.annotation.Placeholder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceDeliveryReportItemData {
    @Placeholder
    private String serviceName;
    @Placeholder
    private String amount;
    @Placeholder
    private String rate;
    @Placeholder
    private String commissionAmount;
}
