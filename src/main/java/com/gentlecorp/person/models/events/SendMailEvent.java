package com.gentlecorp.person.models.events;

import com.gentlecorp.person.models.entities.Person;

import java.time.LocalDate;
import java.util.Map;

public record SendMailEvent(
    String email,
    Map<String, String> placeholders
) {
    public static SendMailEvent toCreateEvent(final Person person, final String role) {
        return new SendMailEvent(
            person.getEmail(),
            Map.of(
                "id", person.getId().toString(),
                "firstName", person.getFirstName(),
                "lastName", person.getLastName(),
                "role", role,
                "accountId", "n/a",
                "cartItemCount", "n/a"
            )
        );
    }

    public static SendMailEvent toDeleteEvent(final Person person) {
        return new SendMailEvent(
            person.getEmail(),
            Map.of(
                "id", person.getId().toString(),
                "firstName", person.getFirstName(),
                "lastName", person.getLastName(),
                "deletionDate", LocalDate.now().toString(),
                "cartItemCount", "n/a"
            )
        );
    }
}
