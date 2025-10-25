package com.keycloak.spis.common.utils;

import java.util.Arrays;
import java.util.function.Function;

public class EnumUtils {
    public static <T extends Enum<T>> boolean isValidEnumValue(Class<T> enumClass, String name) {
        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(enumValue -> enumValue.name().equals(name));
    }

    public static <T extends Enum<T>> boolean isValidEnumValue(Class<T> enumClass, String value,
            Function<T, String> valueExtractor) {
        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(enumValue -> valueExtractor.apply(enumValue).equals(value));
    }
}
