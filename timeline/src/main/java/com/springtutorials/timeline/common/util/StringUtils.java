package com.springtutorials.timeline.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public final class StringUtils {

    @Nullable
    public static String nullSafeConcat(@Nullable String str1, @Nullable String str2) {
        if (str2 != null) {
            return str1 == null ? str2 : str1 + "\n" + str2;
        } else {
            return str1;
        }
    }

    public static String removeFileNameExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
