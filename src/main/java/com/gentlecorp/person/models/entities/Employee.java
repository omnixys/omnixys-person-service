package com.gentlecorp.person.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Repräsentiert einen Kunden im System.
 * <p>
 * Diese Entität wird in der MongoDB in der Collection 'Customer' gespeichert.
 * </p>
 *
 * @since 26.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    /**
     * Abteilung des Mitarbeiters.
     */
    private String department;
    /**
     * Gehalt des Mitarbeiters.
     */
    private Double salary;
    /**
     * Anstellungsdatum des Mitarbeiters.
     */
    private LocalDate hireDate;
    /**
     * Gibt an, ob der Mitarbeiter extern ist.
     * `false` = interner Mitarbeiter, `true` = externer Mitarbeiter.
     */
    private boolean isExternal;

    /**
     * Rolle des Mitarbeiters im Unternehmen (Admin, User, Manager).
     */
    private String role;

    /**
     * Position des Mitarbeiters (z. B. Abteilungsleiter, Frontend-Entwickler).
     */
    private String position;
}
