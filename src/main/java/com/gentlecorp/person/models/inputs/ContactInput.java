package com.gentlecorp.person.models.dto;

import com.gentlecorp.person.models.entities.Contact;
import com.gentlecorp.person.models.enums.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Eingabe-Datenstruktur für die Erstellung eines Kontakts.
 * <p>
 * Wird für GraphQL- oder REST-Mutationen verwendet. Die Validierung erfolgt hier vollständig
 * und die Umwandlung zur {@link Contact}-Entität ist enthalten.
 * </p>
 *
 * @param lastName         Nachname des Kontakts (Pflichtfeld)
 * @param firstName        Vorname des Kontakts (Pflichtfeld)
 * @param relationship     Beziehung zur Person (Pflichtfeld)
 * @param withdrawalLimit  Auszahlungslimit
 * @param emergencyContact Markierung als Notfallkontakt
 * @param startDate        Startdatum der Beziehung
 * @param endDate          Enddatum der Beziehung
 *
 * @since 13.02.2025
 * @version 1.0
 */
public record ContactInput(
    @NotBlank(message = "Nachname darf nicht leer sein.")
    String lastName,

    @NotBlank(message = "Vorname darf nicht leer sein.")
    String firstName,

    @NotNull(message = "Beziehung darf nicht null sein.")
    RelationshipType relationship,

    int withdrawalLimit,

    boolean emergencyContact,

    LocalDate startDate,

    LocalDate endDate
) {
    /**
     * Konvertiert das Eingabeobjekt in ein {@link Contact}-Dokument für MongoDB.
     *
     * @return neue Contact-Entität
     */
    public Contact toEntity() {
        LocalDateTime now = LocalDateTime.now();
        return new Contact(
            UUID.randomUUID(),     // id
            0,                     // version
            lastName,
            firstName,
            relationship,
            withdrawalLimit,
            emergencyContact,
            startDate,
            endDate,
            now,                  // created
            now                   // updated
        );
    }
}
