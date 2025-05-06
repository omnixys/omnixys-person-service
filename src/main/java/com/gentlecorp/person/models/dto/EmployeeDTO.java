package com.gentlecorp.person.models.dto;

import com.gentlecorp.person.models.entities.Employee;
import com.gentlecorp.person.models.enums.EmployeePosition;
import com.gentlecorp.person.models.enums.EmployeeRole;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Eingabeobjekt zur Erstellung eines neuen Mitarbeiterprofils.
 * <p>
 * Dieses Objekt enthält alle relevanten Angaben zur Beschäftigung einer Person.
 * </p>
 *
 * @param department     Abteilung des Mitarbeiters
 * @param salary         Gehalt (mind. 0.0)
 * @param hireDate       Anstellungsdatum
 * @param isExternal     true = externer Mitarbeiter
 * @param role           Rolle im Unternehmen (ADMIN, MANAGER, USER)
 * @param position       Position im Unternehmen (z. B. DEVOPS_ENGINEER)
 *
 * @since 27.02.2025
 * @version 1.0
 */
public record EmployeeDTO(
    @NotBlank(message = "Abteilung darf nicht leer sein.")
    String department,

    @DecimalMin(value = "0.0", inclusive = true, message = "Gehalt darf nicht negativ sein.")
    Double salary,

    @NotNull(message = "Anstellungsdatum darf nicht null sein.")
    LocalDate hireDate,

    boolean isExternal,

    @NotNull(message = "Rolle darf nicht null sein.")
    EmployeeRole role,

    @NotNull(message = "Position darf nicht null sein.")
    EmployeePosition position
) {
    /**
     * Wandelt dieses Eingabeobjekt in ein {@link Employee}-Objekt.
     *
     * @return neues {@link Employee}-Objekt.
     */
    public Employee toEntity() {
        return Employee.builder()
            .department(department)
            .salary(salary)
            .hireDate(hireDate)
            .isExternal(isExternal)
            .role(role.name())
            .position(position.name())
            .build();
    }
}
