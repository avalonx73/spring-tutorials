package com.springtutorials.generatedocx.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MoneyAmountConverterTest {

    @Test
    void convertToWords() {
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1.09)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1_001.97)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1_019_017.63)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1_011_012_013.17)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(22222.854)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(13001.41)));
    }
}