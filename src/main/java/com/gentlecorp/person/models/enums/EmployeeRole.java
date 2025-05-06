package com.gentlecorp.person.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * Definiert die administrativen Rechte eines Mitarbeiters.
 *
 * @since 26.02.2025
 * @author Caleb Gyamfi
 * @version 1.0
 */
@RequiredArgsConstructor
public enum EmployeeRole {
    ADMIN("A", "ADMIN"),       // Hat volle Rechte im System
    MANAGER("M", "MANAGER"),   // Hat Managementrechte
    USER("U", "USER");         // Normaler Mitarbeiter mit begrenzten Rechten

    private final String shortType;
    private final String longType;

    /**
     * Gibt die JSON-kompatible String-Repräsentation zurück.
     *
     * @return Die Langform der Rolle (z. B. "ADMIN").
     */
    @JsonValue
    public String getJsonValue() {
        return longType;
    }

    /**
     * Wandelt einen String-Wert in den entsprechenden Enum-Wert um.
     * Unterstützt sowohl Kurzformen ("A", "M", "U") als auch Langformen ("ADMIN", "MANAGER", "USER").
     *
     * @param value der String-Wert des EmployeeRole.
     * @return der entsprechende Enum-Wert.
     * @throws IllegalArgumentException wenn der Wert ungültig ist.
     */
    @JsonCreator
    public static EmployeeRole of(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("EmployeeRole darf nicht null sein.");
        }

        String upperValue = value.toUpperCase();
        return Stream.of(values())
            .filter(role -> role.shortType.equalsIgnoreCase(upperValue) || role.longType.equalsIgnoreCase(upperValue))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Ungültiger Wert '%s' für EmployeeRole. Erlaubt: A,M,U oder ADMIN, MANAGER, USER", value)
            ));
    }
}
