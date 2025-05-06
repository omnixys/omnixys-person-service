package com.gentlecorp.person.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * Definiert die Position eines Mitarbeiters im Unternehmen.
 *
 * @since 26.02.2025
 * @author Caleb Gyamfi
 * @version 1.0
 */
@RequiredArgsConstructor
public enum EmployeePosition {
    ABTEILUNGSLEITER("AL", "ABTEILUNGSLEITER"),
    TEAMLEITER("TL", "TEAMLEITER"),
    FRONTEND_DEVELOPER("FD", "FRONTEND_DEVELOPER"),
    BACKEND_DEVELOPER("BD", "BACKEND_DEVELOPER"),
    DEVOPS_ENGINEER("DE", "DEVOPS_ENGINEER"),
    CONSULTANT("C", "CONSULTANT"),
    HR_MANAGER("HR", "HR_MANAGER");

    private final String shortType;
    private final String longType;

    /**
     * Gibt die JSON-kompatible String-Repräsentation zurück.
     *
     * @return Die Langform der Position (z. B. "FRONTEND_DEVELOPER").
     */
    @JsonValue
    public String getJsonValue() {
        return longType;
    }

    /**
     * Wandelt einen String-Wert in den entsprechenden Enum-Wert um.
     * Unterstützt sowohl Kurzformen ("AL", "FD", "BD") als auch Langformen ("ABTEILUNGSLEITER", "FRONTEND_DEVELOPER").
     *
     * @param value der String-Wert des EmployeePosition.
     * @return der entsprechende Enum-Wert.
     * @throws IllegalArgumentException wenn der Wert ungültig ist.
     */
    @JsonCreator
    public static EmployeePosition of(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("EmployeePosition darf nicht null sein.");
        }

        String upperValue = value.toUpperCase();
        return Stream.of(values())
            .filter(position -> position.shortType.equalsIgnoreCase(upperValue) || position.longType.equalsIgnoreCase(upperValue))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Ungültiger Wert '%s' für EmployeePosition. Erlaubt: AL,FD,BD oder ABTEILUNGSLEITER, FRONTEND_DEVELOPER, ...", value)
            ));
    }
}
