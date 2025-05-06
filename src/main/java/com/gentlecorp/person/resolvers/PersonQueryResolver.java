package com.gentlecorp.person.resolvers;

import com.gentlecorp.person.exceptions.AccessForbiddenException;
import com.gentlecorp.person.exceptions.NotFoundException;
import com.gentlecorp.person.messaging.KafkaPublisherService;
import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.models.inputs.FilterInput;
import com.gentlecorp.person.models.inputs.PaginationInput;
import com.gentlecorp.person.models.inputs.SortInput;
import com.gentlecorp.person.security.CustomUserDetails;
import com.gentlecorp.person.services.PersonReadService;
import com.gentlecorp.person.services.PersonWriteService;
import com.gentlecorp.person.tracing.LoggerPlus;
import com.gentlecorp.person.tracing.LoggerPlusFactory;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.gentlecorp.person.models.enums.PersonType.CUSTOMER;
import static com.gentlecorp.person.models.enums.PersonType.EMPLOYEE;
import static org.springframework.graphql.execution.ErrorType.FORBIDDEN;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

/**
 * Der `QueryController` verarbeitet GraphQL-Anfragen für Kunden und Benutzer.
 * Er stellt Methoden für das Abrufen von Kunden und anderen Benutzerdaten bereit.
 *
 * @since 13.02.2024
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.1
 */
@Controller
@RequiredArgsConstructor
public class PersonQueryResolver {

    private final PersonReadService personReadService;
    private final LoggerPlusFactory factory;
    private LoggerPlus logger() {
        return factory.getLogger(getClass());
    }

    /**
     * Ruft einen Kunden anhand seiner ID ab.
     *
     * @param id Die UUID des Kunden.
     * @param authentication Die Authentifizierungsinformationen des Nutzers.
     * @return Das gefundene `Customer`-Objekt.
     */
    @QueryMapping("customer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SUPREME', 'ELITE', 'BASIC')")
    Person getCustomerById(
        @Argument final UUID id,
        final Authentication authentication
    ) {
        return getPerson(id, authentication);
    }

    @QueryMapping("employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SUPREME', 'ELITE', 'BASIC')")
    Person getEmployeeById(
        @Argument final UUID id,
        final Authentication authentication
    ) {
        return getPerson(id, authentication);
    }

    /**
     * GraphQL-Query für `customers`.
     *
     * @param filter     Die Filterbedingungen als `FilterInput`.
     * @param pagination Die Paginierungsparameter.
     * @param order      Die Sortierkriterien.
     * @return Eine Liste der gefundenen Kunden.
     */
    @QueryMapping("customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Collection<Person> findCustomers(
        @Argument FilterInput filter,
        @Argument PaginationInput pagination,
        @Argument SortInput order
    ) {
        logger().debug("findCustomers: filter={}, pagination={}, order={}", filter, pagination, order);

        // ✅ Konvertiere den Filter in eine Map für MongoDB
        final Map<String, Object> filterMap = filter != null ? filter.toMap() : new HashMap<>();
        logger().debug("findCustomers: filterMap={}", filterMap);

        // ✅ Konvertiere die Sortierung in Map<String, String>
        Map<String, String> sortMap = order != null
            ? Map.of(order.field().name(), order.direction().name())
            : Map.of();
        logger().debug("findCustomers: sortMap={}", sortMap);

        // ✅ Falls keine Paginierung angegeben → Alle Daten zurückgeben
        int page = pagination != null ? pagination.offset()-1 : 0;
        int size = pagination != null ? pagination.limit() : Integer.MAX_VALUE;
        logger().debug("findCustomers: page={}, size={}", page, size);

        final var people =  personReadService.find(filterMap, page, size, sortMap);
        final var customers = people.stream()
            .filter(person -> person.getPersonType() == CUSTOMER)
            .toList();
        logger().debug("findCustomer: employees={}", customers);
        return customers;
    }

    @QueryMapping("employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Collection<Person> findEmployees(
        @Argument FilterInput filter,
        @Argument PaginationInput pagination,
        @Argument SortInput order
    ) {
        logger().debug("findEmployees: filter={}, pagination={}, order={}", filter, pagination, order);

        // ✅ Konvertiere den Filter in eine Map für MongoDB
        final Map<String, Object> filterMap = filter != null ? filter.toMap() : new HashMap<>();
        logger().debug("findEmployees: filterMap={}", filterMap);

        // ✅ Konvertiere die Sortierung in Map<String, String>
        Map<String, String> sortMap = order != null
            ? Map.of(order.field().name(), order.direction().name())
            : Map.of();
        logger().debug("findEmployees: sortMap={}", sortMap);

        // ✅ Falls keine Paginierung angegeben → Alle Daten zurückgeben
        int page = pagination != null ? pagination.offset()-1 : 0;
        int size = pagination != null ? pagination.limit() : Integer.MAX_VALUE;
        logger().debug("findEmployees: page={}, size={}", page, size);

        final var people =  personReadService.find(filterMap, page, size, sortMap);
        final var employees = people.stream()
            .filter(person -> person.getPersonType() == EMPLOYEE)
            .toList();
        logger().debug("findEmployees: employees={}", employees);
        return employees;
    }


    @QueryMapping("hallo")
    public String hello() {
        return "Hello, GraphQL!";
    }

    /**
     * Behandelt eine `AccessForbiddenException` und gibt ein entsprechendes GraphQL-Fehlerobjekt zurück.
     *
     * @param ex Die ausgelöste Ausnahme.
     * @param env Das GraphQL-Umfeld für Fehlerinformationen.
     * @return Ein `GraphQLError` mit der Fehlerbeschreibung.
     */
    @GraphQlExceptionHandler
    GraphQLError onAccessForbidden(final AccessForbiddenException ex, DataFetchingEnvironment env) {
        return GraphQLError.newError()
            .errorType(FORBIDDEN)
            .message(ex.getMessage())
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .build();
    }

    /**
     * Behandelt eine `NotFoundException` und gibt ein entsprechendes GraphQL-Fehlerobjekt zurück.
     *
     * @param ex Die ausgelöste Ausnahme.
     * @param env Das GraphQL-Umfeld für Fehlerinformationen.
     * @return Ein `GraphQLError` mit der Fehlerbeschreibung.
     */
    @GraphQlExceptionHandler
    GraphQLError onNotFound(final NotFoundException ex, DataFetchingEnvironment env) {
        return GraphQLError.newError()
            .errorType(NOT_FOUND)
            .message(ex.getMessage())
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .build();
    }

    @NotNull
    private Person getPerson(@Argument UUID id, Authentication authentication) {
        logger().debug("getCustomerById: id={}", id);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        final var customer = personReadService.findById(id,user);
        logger().debug("getCustomerById: customer={}", customer);
        return customer;
    }
}
