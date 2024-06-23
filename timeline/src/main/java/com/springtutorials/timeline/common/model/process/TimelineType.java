package com.springtutorials.timeline.common.model.process;

public enum TimelineType {

    PROVIDER_SETTLEMENTS("Процес розрахунків з постачальниками"),
    REFUND_WAY4_PAYMENTS("Процес повернення карткових платежів"),
    CLIENT_REFUND_WAY4_PAYMENTS("Процес повернення карткових платежів"),
    PROVIDER_BILLING("Процес утримання комісії з постачальників"),

    DUMMY_TIMELINE("Таймайн для отладки");
    private final String description;

    TimelineType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
