package com.gentlecorp.person.models.dto;

import com.gentlecorp.person.models.enums.GenderType;
import com.gentlecorp.person.utils.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static com.gentlecorp.person.utils.Constants.EMAIL_MAX_LENGTH;
import static com.gentlecorp.person.utils.Constants.FIRST_NAME_PATTERN;
import static com.gentlecorp.person.utils.Constants.LAST_NAME_PATTERN;
import static com.gentlecorp.person.utils.Constants.NAME_MAX_LENGTH;
import static com.gentlecorp.person.utils.Constants.PHONE_NUMBER_PATTERN;
import static com.gentlecorp.person.utils.Constants.USERNAME_MAX_LENGTH;
import static com.gentlecorp.person.utils.Constants.USERNAME_PATTERN;

/**
 * Eingabestruktur zur Erstellung einer neuen Person.
 *
 * @param firstName   Vorname (Pflicht)
 * @param lastName    Nachname (Pflicht)
 * @param email       E-Mail-Adresse (Pflicht, eindeutig)
 * @param phoneNumber Telefonnummer (Pflicht)
 * @param birthdate   Geburtsdatum (Vergangenheit)
 * @param gender      Geschlecht (Pflicht)
 * @param address     Adresse (Pflicht)
 *
 * @since 18.04.2025
 * @version 1.0
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 */
public record PersonDTO(
    @NotBlank(message = "Bitte gib deinen Nachnamen an.")
    @Pattern(regexp = LAST_NAME_PATTERN, message = "Der Nachname darf nur Buchstaben enthalten und sollte mit einem großen Buchstaben anfangen.")
    @Size(max = NAME_MAX_LENGTH, message = "Der Nachname darf maximal {max} Zeichen lang sein.") String lastName,

    @NotBlank(message = "Bitte gib deinen Vornamen an.")
    @Pattern(regexp = FIRST_NAME_PATTERN, message = "Der Vorname darf nur Buchstaben enthalten und sollte mit einem großen Buchstaben anfangen.")
    @Size(max = NAME_MAX_LENGTH, message = "Der Vorname darf maximal {max} Zeichen lang sein.")
    String firstName,

    @NotBlank(message = "Bitte gib deine E-Mail-Adresse an.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse an.")
    @Size(max = EMAIL_MAX_LENGTH, message = "Die E-Mail darf maximal {max} Zeichen lang sein.")
    String email,

    @Pattern(regexp = PHONE_NUMBER_PATTERN, message = "Bitte gib eine gültige Telefonnummer an. Die Telefonnummer muss zwischen 7 und 25 Zeichen lang sein.")
    String phoneNumber,

    @Past(message = "Das Geburtsdatum muss in der Vergangenheit liegen.")
    @NotNull(message = "Das Geburtsdatum ist erforderlich.")
    LocalDate birthdate,

    @NotNull(message = "Bitte gib dein Geschlecht an.")
    GenderType gender,

    @NotBlank(groups = Constants.OnCreate.class, message = "Bitte gib deine Adresse an.")
    @Valid
    AddressDTO address
) {
}
