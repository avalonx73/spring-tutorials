package com.springtutorials.timeline.common.model.provider;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class ProviderRegistryRecordMeter {
    private Integer orderNumber;
    private String serialNumber;
    private Integer zone = 0;
    private Double tariffZone1;
    private Double priorReadingValueZone1;
    private Double currentReadingValueZone1;
    private Double tariffZone2;
    private Double priorReadingValueZone2;
    private Double currentReadingValueZone2;
    private Double tariffZone3;
    private Double priorReadingValueZone3;
    private Double currentReadingValueZone3;

    @Nullable
    public Double getTariffByZone(int zoneId) {
        Double zoneTariff = null;
        if (zoneId == 1) {
            zoneTariff = getTariffZone1();
        } else if (zoneId == 2) {
            zoneTariff = getTariffZone2();
        } else if (zoneId == 3) {
            zoneTariff = getTariffZone3();
        }
        return zoneTariff;
    }

    @Nullable
    public Double getPreviousValueByZone(int zoneId) {
        Double previousValue = null;
        if (zoneId == 1) {
            previousValue = getPriorReadingValueZone1();
        } else if (zoneId == 2) {
            previousValue = getPriorReadingValueZone2();
        } else if (zoneId == 3) {
            previousValue = getPriorReadingValueZone3();
        }
        return previousValue;
    }

    @Nullable
    public Double getCurrentValueByZone(int zoneId) {
        Double currentValue = null;
        if (zoneId == 1) {
            currentValue = getCurrentReadingValueZone1();
        } else if (zoneId == 2) {
            currentValue = getCurrentReadingValueZone2();
        } else if (zoneId == 3) {
            currentValue = getCurrentReadingValueZone3();
        }
        return currentValue;
    }

    public void setTariff(Integer zoneId, Double value) {
        if (zoneId == 1) {
            setTariffZone1(value);
        } else if (zoneId == 2) {
            setTariffZone2(value);
        } else if (zoneId == 3) {
            setTariffZone3(value);
        }
    }

    public void setPreviousReading(Integer zoneId, Double value) {
        if (zoneId == 1) {
            setPriorReadingValueZone1(value);
        } else if (zoneId == 2) {
            setPriorReadingValueZone2(value);
        } else if (zoneId == 3) {
            setPriorReadingValueZone3(value);
        }
    }

    public void setCurrentReading(Integer zoneId, Double value) {
        if (zoneId == 1) {
            setCurrentReadingValueZone1(value);
        } else if (zoneId == 2) {
            setCurrentReadingValueZone2(value);
        } else if (zoneId == 3) {
            setCurrentReadingValueZone3(value);
        }
    }
}
