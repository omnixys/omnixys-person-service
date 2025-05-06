package com.gentlecorp.person.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Definiert den Typ einer Person (Kunde oder Mitarbeiter).
 *
 * @since 26.02.2025
 * @author Caleb Gyamfi
 * @version 1.0
 */
@RequiredArgsConstructor
public enum PersonType {
    CUSTOMER("C", "CUSTOMER"), // Kunde
    EMPLOYEE("E", "EMPLOYEE");  // Mitarbeiter

    private final String shortType;
    private final String longType;

    /**
     * Gibt die JSON-kompatible String-Repräsentation zurück.
     *
     * @return Die Langform der Kontaktoption (z. B. "EMAIL").
     */
    @JsonValue
    public String getJsonValue() {
        return longType; // Gibt die Langform aus, z. B. "EMAIL"
    }

    private static final Map<String, PersonType> lookup = Stream.of(values())
        .collect(java.util.stream.Collectors.toMap(
            e -> e.shortType.toUpperCase(), e -> e
        ));

    /**
     * Wandelt einen String-Wert in den entsprechenden Enum-Wert um.
     * Unterstützt sowohl "A", "B", "I", "C" als auch "ACTIVE", "BLOCKED", "INACTIVE", "CLOSED".
     *
     * @param value der String-Wert des Status.
     * @return der entsprechende Enum-Wert.
     * @throws IllegalArgumentException wenn der Wert ungültig ist.
     */
    @JsonCreator
    public static PersonType of(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("PersonType darf nicht null sein.");
        }

        String upperValue = value.toUpperCase();
        for (PersonType type : values()) {
            if (type.shortType.equalsIgnoreCase(upperValue) || type.longType.equalsIgnoreCase(upperValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
            String.format("Ungültiger wert '%s' für PersonType. Erlaubt: C,E oder CUSTOMER, EMPLOYEE", value)
        );
    }
}
