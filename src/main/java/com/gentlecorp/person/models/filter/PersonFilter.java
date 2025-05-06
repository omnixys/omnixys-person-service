package com.gentlecorp.person.models.filter;

import com.gentlecorp.person.models.enums.GenderType;
import com.gentlecorp.person.models.enums.MaritalStatusType;
import com.gentlecorp.person.models.enums.PersonType;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Typisiertes Filterobjekt für Person-Abfragen.
 */
@Builder
public record PersonFilter(
    @FilterField(operator = FilterOperator.IS)
    String firstName,

    @FilterField(operator = FilterOperator.REGEX, path = "lastName")
    String lastNameRegex,

    @FilterField
    String email,

    @FilterField
    GenderType gender,

    @FilterField(path = "customer.maritalStatus")
    MaritalStatusType maritalStatus,

    @FilterField
    PersonType personType,

    @FilterField(operator = FilterOperator.GT, path = "birthdate")
    LocalDate birthdateAfter,

    @FilterField(operator = FilterOperator.LT, path = "birthdate")
    LocalDate birthdateBefore
) {}
