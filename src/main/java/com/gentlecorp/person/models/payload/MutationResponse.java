package com.gentlecorp.person.models.payload;

import com.gentlecorp.person.models.entities.Person;

import java.util.List;

public record MutationResponse(
    boolean success,
    String message,
    Person result,
    int affectedCount,
    List<String> warnings
) {
}
