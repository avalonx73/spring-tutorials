package com.springtutorials.generatedocx.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MoneyAmountConverterTest {

    @Test
    void convertToWords() {
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1_000_000_000.01)));
       log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1_000_000.97)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(1_000.63)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(100.17)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(22222.854)));
        log.info(MoneyAmountConverter.convertToUkrWords(BigDecimal.valueOf(13001.41)));
    }
}