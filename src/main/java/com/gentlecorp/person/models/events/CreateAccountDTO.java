package com.gentlecorp.person.models.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Datentransferobjekt (DTO) für Kontoinformationen.
 * <p>
 * Enthält Informationen zu Saldo, Kategorie, Zinssatz, Überziehungslimit und Abhebelimit.
 * </p>
 *
 * @param balance           Der Kontostand.
 * @param category          Die Kategorie des Kontos.
 * @param rateOfInterest    Der Zinssatz des Kontos.
 * @param overdraft         Das Überziehungslimit des Kontos.
 * @param withdrawalLimit   Das Abhebelimit des Kontos.
 * @param userId       Die ID des zugehörigen Kunden.
 *
 * @since 13.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
public record CreateAccountDTO(
    BigDecimal balance,
    String category,
    int rateOfInterest,
    int overdraft,
    int withdrawalLimit,
    UUID userId,

    int transactionLimit,
    String username
) {
}
