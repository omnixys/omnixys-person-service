package com.gentlecorp.person.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import static com.gentlecorp.person.utils.Constants.USERNAME_PATTERN;

public record UserDTO(
    @NotBlank(message = "Bitte gib einen Benutzernamen an.")
    @Pattern(regexp = USERNAME_PATTERN, message = "Der Benutzername muss zwischen 4 und 20 Zeichen lang sein. Der Benutzername darf nur Buchstaben, Zahlen, Unterstriche, Punkte oder Bindestriche enthalten.")
    String username,

    @NotBlank(message = "Bitte gib ein Passwort an.")
    String password
) {
}
