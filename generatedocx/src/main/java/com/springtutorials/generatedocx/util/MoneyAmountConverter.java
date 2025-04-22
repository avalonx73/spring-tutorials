package com.springtutorials.generatedocx.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MoneyAmountConverter {

    private static final String[] maleUnits = {"", "один", "два", "три", "чотири", "п'ять", "шість", "сім", "вісім", "дев'ять"};
    private static final String[] femaleUnits = {"", "одна", "дві", "три", "чотири", "п'ять", "шість", "сім", "вісім", "дев'ять"};
    private static final String[] teens = {"десять", "одиннадцять", "дванадцять", "тринадцять", "чотирнадцять",
            "п'ятнадцять", "шістнадцять", "сімнадцять", "вісімнадцять", "дев'ятнадцять"};
    private static final String[] tens = {"", "", "двадцять", "тридцять", "сорок", "п'ятдесят", "шістдесят",
            "сімдесят", "вісімдесят", "дев'яносто"};
    private static final String[] hundreds = {"", "сто", "двісті", "триста", "чотириста", "п'ятсот", "шістсот",
            "сімсот", "вісімсот", "дев'ятсот"};

    public static String convertToUkrWords(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        String result = "";

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            result = "мінус ";
        }

        amount = amount.abs().setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(new BigDecimal("999999999999.99")) > 0) {
            return "Занадто велике число";
        }

        long currencyUnitValue = amount.longValue();
        long currencySubunitValue = amount.remainder(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        if (currencyUnitValue == 0) {
            result = "нуль гривень ";
        } else {
            result += convertNumberToWords(currencyUnitValue);
        }

        result += convertCurrencySubunitToWords(currencySubunitValue);

        return result.trim();
    }

    private static String convertNumberToWords(long number) {
        if (number == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        long billions = number / 1_000_000_000;
        long millions = (number % 1_000_000_000) / 1_000_000;
        long thousands = (number % 1_000_000) / 1_000;
        long remainder = number % 1_000;

        if (billions > 0) {
            result.append(convertGroup(billions, maleUnits))
                    .append(getWordForBillions(billions))
                    .append(" ");
        }

        if (millions > 0) {
            result.append(convertGroup(millions, maleUnits))
                    .append(getWordForMillions(millions))
                    .append(" ");
        }

        if (thousands > 0) {
            result.append(convertGroup(thousands, femaleUnits))
                    .append(getWordForThousands(thousands))
                    .append(" ");
        }

            result.append(convertGroup(remainder, femaleUnits))
                    .append(getWordForCurrencyUnit(remainder))
                    .append(" ");

        return result.toString();
    }

    private static String convertGroup(long number, String[] units) {
        StringBuilder result = new StringBuilder();

        long hundred = number / 100;
        long ten = (number % 100) / 10;
        long unit = number % 10;

        if (hundred > 0) {
            result.append(hundreds[(int)hundred]).append(" ");
        }

        if (ten == 1) {
            result.append(teens[(int)unit]).append(" ");
        } else {
            if (ten > 0) {
                result.append(tens[(int)ten]).append(" ");
            }
            if (unit > 0) {
                result.append(units[(int)unit]).append(" ");
            }
        }

        return result.toString();
    }

    private static String convertCurrencySubunitToWords(long currencySubunit) {
        long ten = (currencySubunit % 100) / 10;
        long unit = currencySubunit % 10;

        StringBuilder result = new StringBuilder();

        if (ten == 1) {
            result.append(teens[(int)unit]).append(" ");
        } else {
            if (ten > 0) {
                result.append(tens[(int)ten]).append(" ");
            }
            if (unit > 0) {
                result.append(femaleUnits[(int)unit]).append(" ");
            }
        }

        if (currencySubunit == 0) {
            return "нуль копійок";
        }

        String currencySubunitStr = result.toString();

        if (currencySubunit >= 11 && currencySubunit <= 19) {
            return currencySubunitStr + "копійок";
        }

        switch ((int)(currencySubunit % 10)) {
            case 1: return currencySubunitStr + "копійка";
            case 2:
            case 3:
            case 4: return currencySubunitStr + "копійки";
            default: return currencySubunitStr + "копійок";
        }
    }

    private static String getWordForCurrencyUnit(long number) {
        if ((number % 100 >= 11 && number % 100 <= 19) || (number >= 11 && number <= 19)) {
            return "гривень";
        }

        switch ((int)(number % 10)) {
            case 1: return "гривня";
            case 2:
            case 3:
            case 4: return "гривні";
            default: return "гривень";
        }
    }

    private static String getWordForThousands(long number) {
        if ((number % 100 >= 11 && number % 100 <= 19) || (number >= 11 && number <= 19)) {
            return "тисяч";
        }

        switch ((int)(number % 10)) {
            case 1: return "тисяча";
            case 2:
            case 3:
            case 4: return "тисячі";
            default: return "тисяч";
        }
    }

    private static String getWordForMillions(long number) {
         if ((number % 100 >= 11 && number % 100 <= 19) || (number >= 11 && number <= 19)) {
            return "міліонів";
        }

        switch ((int)(number % 10)) {
            case 1: return "міліон";
            case 2:
            case 3:
            case 4: return "міліона";
            default: return "міліонів";
        }
    }

    private static String getWordForBillions(long number) {
        if ((number % 100 >= 11 && number % 100 <= 19) || (number >= 11 && number <= 19)) {
            return "мільярдів";
        }

        switch ((int)(number % 10)) {
            case 1: return "мільярд";
            case 2:
            case 3:
            case 4: return "мільярда";
            default: return "мільярдів";
        }
    }

    public static String formatCurrency(BigDecimal value) {
        DecimalFormat formatter = new DecimalFormat("### ###.##");
        formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("uk", "UA")));
        return formatter.format(value);
    }

    public static String formatDate(LocalDate value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'р'", new Locale("uk", "UA"));
        return value.format(formatter);
    }
}
