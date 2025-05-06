package com.gentlecorp.person.models.entities;

import com.gentlecorp.person.models.entities.Customer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Repräsentiert die Adresse eines Kunden.
 * <p>
 * Diese Klasse wird als eingebettetes Attribut in der {@link Customer} -Entität verwendet.
 * </p>
 *
 * @since 13.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    /**
     * Straße der Adresse (Pflichtfeld).
     */
    private String street;

    /**
     * Hausnummer der Adresse (Pflichtfeld).
     */
    private String houseNumber;

    /**
     * Postleitzahl der Adresse (muss aus 5 Ziffern bestehen).
     */
    private String zipCode;

    /**
     * Stadt der Adresse (Pflichtfeld).
     */
    private String city;

    /**
     * Bundesland der Adresse (optional).
     */
    private String state;

    /**
     * Land der Adresse (Pflichtfeld).
     */
    private String country;

    /**
     * Zusätzliche Informationen zur Adresse (optional).
     */
    private String additionalInfo;
}
