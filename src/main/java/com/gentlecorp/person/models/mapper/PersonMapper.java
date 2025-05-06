package com.gentlecorp.person.models.mapper;

import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.models.inputs.CreateCustomerInput;
import com.gentlecorp.person.models.inputs.CreateEmployeeInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper für die Umwandlung von DTOs in Entitäten und umgekehrt.
 * <p>
 * Diese Schnittstelle nutzt MapStruct, um eine Implementierung zur Laufzeit zu generieren.
 * </p>
 *
 * @since 27.02.2025
 * @author Rachel Dwomoh
 * @version 2.0
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonMapper {

    /**
     * Wandelt die Eingabedaten zur Kundenerstellung in eine `Person`-Entität um.
     * Erstellt eine {@link Person}-Entität aus {@link CreateCustomerInput}.
     * Dabei werden zusätzlich die Kundendaten eingebunden.
     *
     * @param input Die Eingabedaten für den Kunden
     * @return Eine vollständige {@link Person}-Entität mit Kundeninformationen.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "personType", expression = "java(com.gentlecorp.person.models.enums.PersonType.CUSTOMER)")
    @Mapping(target = "lastName", source = "personInput.lastName")
    @Mapping(target = "firstName", source = "personInput.firstName")
    @Mapping(target = "email", source = "personInput.email")
    @Mapping(target = "phoneNumber", source = "personInput.phoneNumber")
    @Mapping(target = "birthdate", source = "personInput.birthdate")
    @Mapping(target = "gender", source = "personInput.gender")
    @Mapping(target = "address", source = "personInput.address")
    @Mapping(target = "customer", source = "customerInput")
    @Mapping(target = "username", source = "userInput.username")
    Person toPerson(CreateCustomerInput input);

    /**
     * Wandelt die Eingabedaten zur Kundenerstellung in eine `Person`-Entität um.
     * Erstellt eine {@link Person}-Entität aus {@link CreateCustomerInput}.
     * Dabei werden zusätzlich die Kundendaten eingebunden.
     *
     * @param input Die Eingabedaten für den Kunden
     * @return Eine vollständige {@link Person}-Entität mit Kundeninformationen.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "personType", expression = "java(com.gentlecorp.person.models.enums.PersonType.CUSTOMER)")
    @Mapping(target = "lastName", source = "personInput.lastName")
    @Mapping(target = "firstName", source = "personInput.firstName")
    @Mapping(target = "email", source = "personInput.email")
    @Mapping(target = "phoneNumber", source = "personInput.phoneNumber")
    @Mapping(target = "birthdate", source = "personInput.birthdate")
    @Mapping(target = "gender", source = "personInput.gender")
    @Mapping(target = "address", source = "personInput.address")
    @Mapping(target = "employee", source = "employeeInput")
    @Mapping(target = "username", source = "userInput.username")
    Person toPerson(CreateEmployeeInput input);
}
