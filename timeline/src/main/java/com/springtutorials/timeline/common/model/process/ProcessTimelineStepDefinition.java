package com.springtutorials.timeline.common.model.process;

import lombok.Getter;

public enum ProcessTimelineStepDefinition {

    RECONCILIATION_WITH_WAY4(TimelineType.PROVIDER_SETTLEMENTS, 0, "reconciliation.with.way4"),
    PAYMENT_DOCUMENTS_FORMATION(TimelineType.PROVIDER_SETTLEMENTS, 1, "payment.document.formation"),
    SENDING_DOCUMENTS_TO_CBS(TimelineType.PROVIDER_SETTLEMENTS, 2, "sending.documents.to.cbs"),
    OBTAINING_DOCUMENT_STATUSES_FROM_CBS(TimelineType.PROVIDER_SETTLEMENTS, 3, "obtaining.document.statuses.from.cbs"),
    SENDING_COMMISSION_DOCUMENT_TO_CBS(TimelineType.PROVIDER_SETTLEMENTS, 4, "sending.commission.document.to.cbs"),
    OBTAINING_COMMISSION_DOCUMENT_STATUS_FROM_CBS(TimelineType.PROVIDER_SETTLEMENTS, 5,
            "obtaining.commission.document.status.from.cbs"),
    PAYMENT_REGISTRY_CREATION(TimelineType.PROVIDER_SETTLEMENTS, 6, "payment.registry.creation"),

    /*  WAY4_PAYMENTS_PROCESSING_WITHOUT_IBAN(TimelineType.REFUND_WAY4_PAYMENTS, 0,
              "processing.payments.without.iban"),*/
    WAY4_REFUNDS_FORMATION_TO_CBS(TimelineType.REFUND_WAY4_PAYMENTS, 0, "formation.way4.refunds.to.cbs"),
    WAY4_REFUNDS_SENDING_TO_CBS(TimelineType.REFUND_WAY4_PAYMENTS, 1, "way4.refunds.sending.to.cbs"),
    OBTAINING_REFUND_WAY4_DOCUMENTS_STATUSES_FROM_CBS(TimelineType.REFUND_WAY4_PAYMENTS, 2,
            "obtaining.refund.way4.documents.statuses.from.cbs"),
    WAY4_REFUNDS_REGISTRY_CREATION(TimelineType.REFUND_WAY4_PAYMENTS, 3, "way4.refunds.registry.creation"),

    WAY4_CLIENT_REFUNDS_FORMATION_TO_CBS(TimelineType.CLIENT_REFUND_WAY4_PAYMENTS, 0, "formation.way4.client.refunds.to.cbs"),
    WAY4_CLIENT_REFUNDS_SENDING_TO_CBS(TimelineType.CLIENT_REFUND_WAY4_PAYMENTS, 1, "way4.client.refunds.sending.to.cbs"),
    OBTAINING_CLIENT_REFUND_WAY4_DOCUMENTS_STATUSES_FROM_CBS(TimelineType.CLIENT_REFUND_WAY4_PAYMENTS, 2,
            "obtaining.client.refund.way4.documents.statuses.from.cbs"),

    PROVIDER_BILLING_INVOICES_CREATION(TimelineType.PROVIDER_BILLING, 0, "provider.billing.payment.commission.calculation.and" +
            ".document.formation"),
    PROVIDER_BILLING_SENDING_DOCUMENTS_TO_CBS(TimelineType.PROVIDER_BILLING, 1, "provider.billing.sending" +
            ".documents.to.cbs"),
    PROVIDER_BILLING_OBTAINING_DOCUMENT_STATUSES_FROM_CBS(TimelineType.PROVIDER_BILLING, 2, "provider.billing" +
            ".obtaining.document.statuses.from.cbs"),
    PROVIDER_BILLING_REPORTS_CREATION(TimelineType.PROVIDER_BILLING, 3, "provider.billing.report.creation"),

    DUMMY_TIMELINE_STEP1(TimelineType.DUMMY_TIMELINE, 0, "dummy.timeline.step1"),
    DUMMY_TIMELINE_STEP2(TimelineType.DUMMY_TIMELINE, 1, "dummy.timeline.step2"),
    DUMMY_TIMELINE_STEP3(TimelineType.DUMMY_TIMELINE, 2, "dummy.timeline.step3"),
    DUMMY_TIMELINE_STEP4(TimelineType.DUMMY_TIMELINE, 3, "dummy.timeline.step4");
    @Getter
    private final TimelineType type;

    @Getter
    private final int order;

    @Getter
    private final String code;

    ProcessTimelineStepDefinition(TimelineType type, int order, String code) {
        this.type = type;
        this.order = order;
        this.code = code;
    }

}
