package com.gentlecorp.person.models.dto;

import com.gentlecorp.person.models.enums.ContactOptionsType;
import com.gentlecorp.person.models.enums.InterestType;
import com.gentlecorp.person.models.enums.MaritalStatusType;
import com.gentlecorp.person.models.entities.Customer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

import static com.gentlecorp.person.utils.Constants.MAX_LEVEL;
import static com.gentlecorp.person.utils.Constants.MIN_LEVEL;

/**
 * Eingabeobjekt zur Erstellung eines neuen Kundenprofils.
 * <p>
 * Wird innerhalb von {@code CreatePersonInput} oder {@code createCustomer} verwendet.
 * </p>
 *
 * @param tierLevel       Kundenstufe (1–10)
 * @param subscribed      Ist der Kunde abonniert?
 * @param maritalStatus   Familienstand
 * @param interests       Interessenliste
 * @param contactOptions  bevorzugte Kontaktmöglichkeiten
 *
 * @since 18.04.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */

public record CustomerDTO(
    @Min(value = MIN_LEVEL, message = "Die Mitgliedschaftsstufe muss mindestens {value} sein.")
    @Max(value = MAX_LEVEL, message = "Die Mitgliedschaftsstufe darf maximal {value} sein.")
    int tierLevel,

    boolean subscribed,

    @NotNull(message = "Bitte gib deinen Familienstand an.")
    MaritalStatusType maritalStatus,

    @UniqueElements(message = "Die Interessen müssen eindeutig sein.")
    List<InterestType> interests,

    @NotNull(message = "Bitte gib mindestens eine bevorzugte Kontaktoption an.")
    @UniqueElements(message = "Die Kontaktoptionen müssen eindeutig sein.")
    List<ContactOptionsType> contactOptions


) {
    /**
     * Wandelt dieses Eingabeobjekt in ein {@link Customer}-Objekt für die Entität {@link com.gentlecorp.person.models.entities.Person}.
     *
     * @return neues {@link Customer}-Objekt.
     */
    public Customer toEntity() {
        return Customer.builder()
            .tierLevel(tierLevel)
            .subscribed(subscribed)
            .maritalStatus(maritalStatus)
            .interests(interests)
            .contactOptions(contactOptions)
            .build();
    }
}
