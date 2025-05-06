package com.gentlecorp.person.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumUtil {
    public static <E extends Enum<E>> String valuesOf(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.joining(", "));
    }
}
