package com.gentlecorp.person.models.events;

import java.util.UUID;

/**
 * Datenübertragungsobjekt für ein neues Kundenkonto, gesendet vom Customer-Service.
 *
 */
public record DeleteAccountDTO(
    UUID id,
    int version,
    String username
) {}
