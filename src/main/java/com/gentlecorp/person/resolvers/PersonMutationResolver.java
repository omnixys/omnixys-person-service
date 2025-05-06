package com.gentlecorp.person.resolvers;

import com.gentlecorp.person.exceptions.AccessForbiddenException;
import com.gentlecorp.person.exceptions.ConstraintViolationsException;
import com.gentlecorp.person.exceptions.ContactExistsException;
import com.gentlecorp.person.exceptions.EmailExistsException;
import com.gentlecorp.person.exceptions.NotFoundException;
import com.gentlecorp.person.exceptions.PasswordInvalidException;
import com.gentlecorp.person.exceptions.UsernameExistsException;
import com.gentlecorp.person.exceptions.VersionAheadException;
import com.gentlecorp.person.exceptions.VersionOutdatedException;
import com.gentlecorp.person.messaging.KafkaPublisherService;
import com.gentlecorp.person.models.dto.ContactDTO;
import com.gentlecorp.person.models.entities.Contact;
import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.models.inputs.CreateCustomerInput;
import com.gentlecorp.person.models.inputs.CreateEmployeeInput;
import com.gentlecorp.person.models.mapper.ContactMapper;
import com.gentlecorp.person.models.mapper.PersonMapper;
import com.gentlecorp.person.security.CustomUserDetails;
import com.gentlecorp.person.services.PersonWriteService;
import com.gentlecorp.person.tracing.LoggerPlus;
import com.gentlecorp.person.tracing.LoggerPlusFactory;
import com.gentlecorp.person.utils.ValidationService;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.gentlecorp.person.exceptions.CustomErrorType.CONFLICT;
import static com.gentlecorp.person.exceptions.CustomErrorType.PRECONDITION_FAILED;
import static org.springframework.graphql.execution.ErrorType.BAD_REQUEST;
import static org.springframework.graphql.execution.ErrorType.FORBIDDEN;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

/**
 * GraphQL-Resolver für schreibende Operationen (Mutationen) rund um Personen.
 *
 * @since 27.02.2025
 * @version 1.0
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 */
@Controller
@RequiredArgsConstructor
public class PersonMutationResolver {

    private final PersonWriteService personWriteService;
    private final PersonMapper personMapper;
    private final ValidationService validation;
    private final ContactMapper contactMapper;
    private final LoggerPlusFactory factory;
    private LoggerPlus logger() {
        return factory.getLogger(getClass());
    }

    /**
     * Erstellt einen neuen Kunden im System und gibt dessen UUID zurück.
     *
     * @param createCustomerInput Eingabedaten für neuen Kunden
     * @return UUID des neu erstellten Kunden
     */

    @MutationMapping("createCustomer")
    UUID createCustomer(
        @Argument("input") final CreateCustomerInput createCustomerInput
    ) {
        logger().debug("createCustomer: personDTO={}, customerDTO={}", createCustomerInput.personInput(), createCustomerInput.customerInput());
        logger().debug("Starte Erstellung eines neuen Kunden: Person={}, Customer={}, User={}",
            createCustomerInput.personInput(),
            createCustomerInput.customerInput(),
            createCustomerInput.userInput().username());

        // DTO-Validierung
        validation.validateDTO(createCustomerInput);

        // Mapping auf Entität
        final var customerInput = personMapper.toPerson(createCustomerInput);
        final var user = createCustomerInput.userInput();
        final var customer = personWriteService.createCustomer(customerInput, user.password() );

        logger().debug("createCustomer: customer={}", customer);
        return customer.getId();
    }

    @MutationMapping("updateCustomer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    Person updateCustomer(
        @Argument("id") final UUID id,
        @Argument("version") final int version,
        @Argument("input") final CreateCustomerInput updateCustomerInput,
        final Authentication authentication
    ) {
        logger().debug("updateCustomer: id={}, version={} updateCustomerInput={}", id, version, updateCustomerInput);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        validation.validateDTO(updateCustomerInput);
        logger().trace("updateCustomer: No constraints violated");

        final var customerInput = personMapper.toPerson(updateCustomerInput);
        final var updatedCustomer = personWriteService.updateCustomer(customerInput, id, version, user);

        logger().debug("updateCustomer: customer={}", updatedCustomer);
        return updatedCustomer;
    }

    @MutationMapping("deleteCustomer")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    boolean deleteCustomer(
        @Argument final UUID id,
        @Argument final int version,
        final Authentication authentication
    ) {
        logger().debug("deleteCustomer: id={}, version={}", id, version);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        personWriteService.deleteCustomerById(id, version, user);
        return true;
    }


    @MutationMapping("createEmployee")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    UUID createEmployee(
        @Argument("input") final CreateEmployeeInput createEmployeeInput
    ) {
        logger().debug("createEmployee: personDTO={}, employeeDTO={}", createEmployeeInput.personInput(), createEmployeeInput.employeeInput());
        final var user = createEmployeeInput.userInput();
        logger().debug("createEmployee: username={}", user.username());
        validation.validateDTO(createEmployeeInput);
        final var employeeInput = personMapper.toPerson(createEmployeeInput);
        final var employee = personWriteService.createEmployee(employeeInput, user.password() );
        logger().debug("createEmployee:employee={}", employee);
        return employee.getId();
    }


    @MutationMapping("updateEmployee")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    Person updateEmployee(
        @Argument("id") final UUID id,
        @Argument("version") final int version,
        @Argument("input") final CreateEmployeeInput updateEmployeeInput,
        final Authentication authentication
    ) {
        logger().debug("updateEmployee: id={}, version={}, updateEmployeeInput={}", id, version, updateEmployeeInput);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        validation.validateDTO(updateEmployeeInput);
        logger().trace("updateEmployee: No constraints violated");
        final var employeeInput = personMapper.toPerson(updateEmployeeInput);
        final var updatedEmployee = personWriteService.updateEmployee(employeeInput, id, version, user);
        logger().debug("updateEmployee: employee={}", updatedEmployee);
        return updatedEmployee;
    }


    @MutationMapping("deleteEmployee")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    boolean deleteEmployee(
        @Argument final UUID id,
        @Argument final int version,
        final Authentication authentication
    ) {
        logger().debug("deleteEmployee: id={}, version={}", id, version);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        personWriteService.deleteEmployeeById(id, version, user);
        return true;
    }



    @MutationMapping("addContact")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SUPREME', 'ELITE', 'BASIC')")
    UUID addContact(
        @Argument("id") final UUID id,
        @Argument("input") final ContactDTO contactDTO,
        final Authentication authentication
    ) {
        logger().debug("addContact: id={}, contactDTO={}", id, contactDTO);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        validation.validateDTO(contactDTO);
        logger().trace("addContact: No constraints violated");

        final var customerInput = contactMapper.toContact(contactDTO);
        final var newContactId = personWriteService.addContact(id, customerInput, user);

        logger().debug("addContact: newContactId={}", newContactId);
        return newContactId;
    }

    @MutationMapping("updateContact")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SUPREME', 'ELITE', 'BASIC')")
    Contact updateContact(
        @Argument("id") final UUID id,
        @Argument("customerVersion") final int customerVersion,
        @Argument("contactId") final UUID contactId,
        @Argument("contactVersion") final int contactVersion,
        @Argument("input") final ContactDTO contactDTO,
        final Authentication authentication
    ) {
        logger().debug("updateContact: id={}, contactDTO={}", id, contactDTO);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        validation.validateDTO(contactDTO);
        logger().trace("updateContact: No constraints violated");

        final var customerInput = contactMapper.toContact(contactDTO);
        final var newContact = personWriteService.updateContact(id, customerVersion, contactId, contactVersion, customerInput, user);

        logger().debug("updateContact: newContact={}", newContact);
        return newContact;

    }

    @MutationMapping("removeContact")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SUPREME', 'ELITE', 'BASIC')")
    boolean removeContact(
        @Argument("id") final UUID id,
        @Argument("customerVersion") final int customerVersion,
        @Argument("contactId") final UUID contactId,
        @Argument("contactVersion") final int contactVersion,
        final Authentication authentication
    ) {
        logger().debug("removeContact: id={}", id);
        final var user = (CustomUserDetails) authentication.getPrincipal();

        return personWriteService.removeContact(id, customerVersion, contactId, contactVersion, user);
    }

    @MutationMapping("updatePassword")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SUPREME', 'ELITE', 'BASIC')")
    boolean updatePassword(
        @Argument("newPassword") final String newPassword,
        final Authentication authentication
    ) {
        logger().debug("updatePassword: newPassword={}", newPassword);
        final var user = (CustomUserDetails) authentication.getPrincipal();
        personWriteService.updatePassword(newPassword, user);
        return true;
    }


    @GraphQlExceptionHandler
    GraphQLError onVersionOutdated(
        final VersionOutdatedException ex,
        final DataFetchingEnvironment env
    ) {
        logger().error("onVersionOutdated: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(PRECONDITION_FAILED)
            .message(ex.getMessage())
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onVersionAhead(
        final VersionAheadException ex,
        final DataFetchingEnvironment env
    ) {
        logger().error("onVersionAhead: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(PRECONDITION_FAILED)
            .message(ex.getMessage())
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onEmailExists(final EmailExistsException ex, final DataFetchingEnvironment env) {
        logger().error("onEmailExists: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(CONFLICT)
            .message("Die Emailadresse " + ex.getEmail() + " existiert bereits.")
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onUsernameExists(final UsernameExistsException ex, final DataFetchingEnvironment env) {
        logger().error("onUsernameExists: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(CONFLICT)
            .message("Der Username " + ex.getUsername() + " existiert bereits.")
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onPasswordInvalid(
        final PasswordInvalidException ex,
        final DataFetchingEnvironment env
    ) {
        logger().error("onPasswordInvalid: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message(ex.getMessage())
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onContactExists(
        final ContactExistsException ex,
        final DataFetchingEnvironment env
    ) {
        logger().debug("onContactExists: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(CONFLICT)
            .message(ex.getMessage())
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .build();
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
        logger().error("onAccessForbidden: {}", ex.getMessage());
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
        logger().error("onNotFound: {}", ex.getMessage());
        return GraphQLError.newError()
            .errorType(NOT_FOUND)
            .message(ex.getMessage())
            .path(env.getExecutionStepInfo().getPath().toList()) // Dynamischer Query-Pfad
            .location(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation()) // GraphQL Location
            .build();
    }

    @GraphQlExceptionHandler
    Collection<GraphQLError> onConstraintViolations(
        final ConstraintViolationsException ex,
        final DataFetchingEnvironment env
    ) {
        logger().error("onConstraintViolations: {}", ex.getMessage());
        return Stream.of(
                Optional.ofNullable(ex.getCustomerViolations()).orElse(List.of()).stream(),
                Optional.ofNullable(ex.getContactViolations()).orElse(List.of()).stream()
            )
            .flatMap(stream -> stream) // Streams korrekt zusammenführen
            .map(violation -> violationToGraphQLError(violation, env))
            .toList(); // Gibt eine fehlerfreie Liste zurück
    }


    private GraphQLError violationToGraphQLError(final ConstraintViolation<?> violation, DataFetchingEnvironment env) {
        logger().debug("violationToGraphQLError: {}", violation);
        // String oder Integer als Listenelement
        final List<Object> path = new ArrayList<>(List.of("input"));

        final var propertyPath = violation.getPropertyPath();
        StreamSupport.stream(propertyPath.spliterator(), false)
            .filter(node -> !Set.of("create", "customer", "contact").contains(node.getName()))
            .forEach(node -> path.add(node.toString()));

        SourceLocation location = Optional.ofNullable(env.getExecutionStepInfo().getField().getSingleField().getSourceLocation())
            .orElse(new SourceLocation(0, 0));  // Fallback auf Zeile 0, Spalte 0

        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message(violation.getMessage())
            .location(location)
            .path(path)
            .build();
    }

}
