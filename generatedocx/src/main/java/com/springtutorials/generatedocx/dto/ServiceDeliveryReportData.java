package com.springtutorials.generatedocx.dto;

import com.springtutorials.generatedocx.annotation.Placeholder;
import lombok.Data;

import java.util.List;

@Data
public class ServiceDeliveryReportData {
    @Placeholder
    private String profileNumber;
    @Placeholder
    private String reportPeriod;
    @Placeholder
    private String periodStartDate;
    @Placeholder
    private String periodEndDate;
    @Placeholder
    private String bankSignerPosition;
    @Placeholder
    private String bankSignerName;
    @Placeholder
    private String bankSigningReason;
    @Placeholder
    private String clientSignerPosition;
    @Placeholder
    private String clientSignerName;
    @Placeholder
    private String clientSigningReason;
    @Placeholder
    private String totalAmount;
    @Placeholder
    private String totalAmountInWords;
    @Placeholder
    private String totalCommissionAmount;
    @Placeholder
    private String totalCommissionAmountInWords;
    @Placeholder
    private String commissionAccount;
    @Placeholder
    private String commissionAccountMfo;
    @Placeholder
    private String commissionAccountEdrpoy;
    @Placeholder
    private String bankName;
    @Placeholder
    private String clientName;
    @Placeholder
    private String footerBankName;
    @Placeholder
    private String footerClientName;
    @Placeholder
    private String bankDetails;
    @Placeholder
    private String clientDetails;
    @Placeholder
    private String footerBankSignerPosition;
    @Placeholder
    private String footerBankSignerName;
    @Placeholder
    private String footerClientSignerPosition;
    @Placeholder
    private String footerClientSignerName;
    private List<ServiceDeliveryReportItemData> serviceData;

/*    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate.toString();
    }

    public void setPeriodEndDate(LocalDate periodEndDate) {
        this.periodEndDate = periodEndDate.toString();
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount.toString();
    }

    public void setTotalAmountInWords(BigDecimal totalAmount) {
        this.totalAmountInWords = totalAmount.toString();
    }

    public void setTotalCommissionAmount(BigDecimal totalCommissionAmount) {
        this.totalCommissionAmount = totalCommissionAmount.toString();
    }

    public void setTotalCommissionAmountInWords(BigDecimal totalCommissionAmount) {
        this.totalCommissionAmountInWords = totalCommissionAmount.toString();
    }*/
}
