package com.gentlecorp.person.services;

import com.gentlecorp.person.exceptions.AccessForbiddenException;
import com.gentlecorp.person.exceptions.EmailExistsException;
import com.gentlecorp.person.exceptions.IllegalArgumentException;
import com.gentlecorp.person.exceptions.NotFoundException;
import com.gentlecorp.person.exceptions.PasswordInvalidException;
import com.gentlecorp.person.exceptions.UsernameExistsException;
import com.gentlecorp.person.messaging.KafkaPublisherService;
import com.gentlecorp.person.models.entities.Contact;
import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.models.enums.PersonType;
import com.gentlecorp.person.repositories.ContactRepository;
import com.gentlecorp.person.repositories.PersonRepository;
import com.gentlecorp.person.security.CustomUserDetails;
import com.gentlecorp.person.security.enums.RoleType;
import com.gentlecorp.person.security.service.KeycloakService;
import com.gentlecorp.person.tracing.LoggerPlus;
import com.gentlecorp.person.tracing.LoggerPlusFactory;
import com.gentlecorp.person.utils.ValidationService;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.gentlecorp.person.messaging.KafkaTopicProperties.TOPIC_NOTIFICATION_CUSTOMER_CREATED;
import static com.gentlecorp.person.messaging.KafkaTopicProperties.TOPIC_NOTIFICATION_CUSTOMER_DELETED;
import static com.gentlecorp.person.models.enums.PersonType.CUSTOMER;
import static com.gentlecorp.person.models.enums.PersonType.EMPLOYEE;
import static com.gentlecorp.person.models.enums.StatusType.ACTIVE;
import static com.gentlecorp.person.security.enums.RoleType.ADMIN;
import static com.gentlecorp.person.utils.Constants.LOWERCASE;
import static com.gentlecorp.person.utils.Constants.MIN_LENGTH;
import static com.gentlecorp.person.utils.Constants.NUMBERS;
import static com.gentlecorp.person.utils.Constants.SYMBOLS;
import static com.gentlecorp.person.utils.Constants.UPPERCASE;
import static java.util.Locale.GERMAN;

/**
 * Service für schreibende Operationen auf Personen.
 * <p>
 * Verwaltet Erstellung und Aktualisierung von Personen, inkl. Validierungen.
 * </p>
 *
 * @author Caleb
 * @since 26.02.2025
 */
@Service
@RequiredArgsConstructor
public class PersonWriteService {

    private final PersonReadService personReadService;
    private final PersonRepository personRepository;
    private final KafkaPublisherService kafkaPublisherService;
    private final KeycloakService keycloakService;
    private final ContactRepository contactRepository;
    private final ValidationService validationService;
    private final Tracer tracer;
    private final LoggerPlusFactory factory;
    private LoggerPlus logger() {
        return factory.getLogger(getClass());
    }

    /**
     * Erstellt eine neue Person im System.
     *
     * @param customer Die zu speichernden Kunden.
     * @return Die gespeicherte Person mit ID und Zeitstempeln.
     * @throws IllegalArgumentException wenn E-Mail oder Username bereits vergeben sind.
     */
    @Observed(name = "person-service.write.create-customer")
    public Person createCustomer(final Person customer, final String password) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.create-customer").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("createCustomer: customer={}", customer);
            validateNewPerson(customer, password, CUSTOMER);
            customer.getCustomer().setCustomerState(ACTIVE);
            customer.setPersonType(CUSTOMER);

            logger().debug(String.format("createCustomer: customer=%s", customer));

            final var role = determineCustomerRole(customer.getCustomer().getTierLevel());
            Span keycloakSpan = tracer.spanBuilder("keycloak.sign-in").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.signIn(customer, password, role);
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler bei SignIn");
                throw e;
            } finally {
                keycloakSpan.end();
            }

            final Person savedCustomer;
            Span mongoSpan = tracer.spanBuilder("person-repository.safe").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                savedCustomer = personRepository.save(customer);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            Span kafkaSpan = tracer.spanBuilder("kafka.send-messages").startSpan();
            try (Scope kafkaScope = kafkaSpan.makeCurrent()) {
                assert kafkaScope != null;
                kafkaPublisherService.sendMail(TOPIC_NOTIFICATION_CUSTOMER_CREATED,savedCustomer, role, false);
                kafkaPublisherService.createAccount(savedCustomer.getId(), savedCustomer.getUsername());
                kafkaPublisherService.createShoppingCart(savedCustomer.getId(),savedCustomer.getUsername());
            } catch (Exception e) {
                kafkaSpan.recordException(e);
                kafkaSpan.setStatus(StatusCode.ERROR, "Fehler beim versenden der Nachrichten");
                throw e;
            } finally {
                kafkaSpan.end();
            }

            logger().warn(String.format("createCustomer: customerDb=%s", savedCustomer));
            logger().trace(String.format("createCustomer: Thread-ID=%s", Thread.currentThread().threadId()));
            return savedCustomer;
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR, "Fehler bei createCustomer");
            throw e;
        } finally {
            serviceSpan.end();
        }
    }

    @Observed(name = "person-service.write.update-customer")
    public Person updateCustomer(Person customerInput, UUID id, int version, final CustomUserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.update-customer").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("updateCustomer: id={}, version={}, customer={}, user={}", id, version, customerInput, user.getUsername());

            final Person customerDb;
            Span readServiceSpan = tracer.spanBuilder("person-service.write.update-customer").startSpan();
            try (Scope readServiceScope = readServiceSpan.makeCurrent()) {
                assert readServiceScope != null;
                customerDb = validateUpdatePerson(id, version, user, CUSTOMER);
            } catch (Exception e) {
                readServiceSpan.recordException(e);
                readServiceSpan.setStatus(StatusCode.ERROR, "Fehler beim lesen");
                throw e;
            } finally {
                readServiceSpan.end();
            }

            validateAndUpdateUserDetails(customerDb, customerInput, user);
            customerDb.set(customerInput);
            customerDb.getCustomer().setCustomerState(ACTIVE);

            Span keycloakSpan = tracer.spanBuilder("keycloak.update-customer").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.update(customerDb, user.getJwt(), isAdmin(user), customerDb.getUsername());
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler beim update");
                throw e;
            } finally {
                keycloakSpan.end();
            }

            final Person updatedCustomerDb;
            Span mongoSpan = tracer.spanBuilder("person-repository.save").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                updatedCustomerDb = personRepository.save(customerDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            logger().debug("updateCustomer: customerDb={}", updatedCustomerDb);
            return updatedCustomerDb;
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR, "Fehler bei createCustomer");
        throw e;
        } finally {
            serviceSpan.end();
        }
    }

    @Observed(name = "person-service.delete-customer")
    public void deleteCustomerById(final UUID id, final int version, final CustomUserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-repository.delete-customer-by-id").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;


            logger().debug("deleteCustomerById: id={}, version={}, user={}", id, version, user.getUsername());

            final var customerDb = validateDeletePerson(id, version, user);
            deleteAssociatedContacts(customerDb);
            logger().debug("deleteCustomerById: kunde wurde gelöscht vom user {}", user.getUsername());


            Span keycloakSpan = tracer.spanBuilder("keycloak.delete").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.delete(user.getToken(), customerDb.getUsername());
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler bei Delete");
                throw e;
            } finally {
                keycloakSpan.end();
            }

            Span mongoSpan = tracer.spanBuilder("person-repository.delete-customer").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                personRepository.delete(customerDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            Span kafkaSpan = tracer.spanBuilder("kafka.send-messages").startSpan();
            try (Scope kafkaScope = kafkaSpan.makeCurrent()) {
                assert kafkaScope != null;
                kafkaPublisherService.deleteShoppingCart(customerDb.getId());
                kafkaPublisherService.deleteAccount(customerDb.getId(), customerDb.getVersion(), customerDb.getUsername());
                kafkaPublisherService.sendMail(TOPIC_NOTIFICATION_CUSTOMER_DELETED,customerDb,null, true);
            } catch (Exception e) {
                kafkaSpan.recordException(e);
                kafkaSpan.setStatus(StatusCode.ERROR, "Fehler beim versenden der Nachrichten");
                throw e;
            } finally {
                kafkaSpan.end();
            }
        }
    }

    @Observed(name = "person-service.create-employee")
    public Person createEmployee(final Person employee, final String password) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.create-employee").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("createEmployee: employee={}", employee);

            validateNewPerson(employee, password, EMPLOYEE);
            employee.setPersonType(EMPLOYEE);
            employee.setEmail(String.format("%s.%s@gentlecorp-systems.com",
                employee.getFirstName(), employee.getLastName()));

            Span keycloakSpan = tracer.spanBuilder("keycloak.sign-in").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.signIn(employee, password, employee.getEmployee().getRole());
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler bei Keycloak-SignIn");
                throw e;
            } finally {
                keycloakSpan.end();
            }

            Person employeeDb;
            Span mongoSpan = tracer.spanBuilder("person-repository.save-employee").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                employeeDb = personRepository.save(employee);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim Speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            logger().debug("createEmployee: employeeDb={}", employeeDb);
            return employeeDb;
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR, "Fehler bei createEmployee");
            throw e;
        } finally {
            serviceSpan.end();
        }
    }

    @Observed(name = "person-service.update-employee")
    public Person updateEmployee(Person employeeInput, UUID id, int version, final CustomUserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.update-employee").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("updateEmployee: id={}, version={}, employee={}, user={}", id, version, employeeInput, user.getUsername());

            final var employeeDb = validateUpdatePerson(id, version, user, EMPLOYEE);
            validateAndUpdateUserDetails(employeeDb, employeeInput, user);
            employeeDb.set(employeeInput);

            Span keycloakSpan = tracer.spanBuilder("keycloak.update-employee").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.update(employeeDb, user.getJwt(), isAdmin(user), employeeDb.getUsername());
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler bei Keycloak-SignIn");
                throw e;
            } finally {
                keycloakSpan.end();
            }

            final Person updatedEmployeeDb;
            Span mongoSpan = tracer.spanBuilder("person-repository.update-employee").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                updatedEmployeeDb = personRepository.save(employeeDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim Speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            logger().debug("updateEmployee: employeeDb={}, user={}", updatedEmployeeDb, user.getUsername());
            return updatedEmployeeDb;
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            serviceSpan.end();
        }
    }

    @Observed(name = "person-service.delete-employee")
    public void deleteEmployeeById(final UUID id, final int version, final CustomUserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.delete-employee").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("deleteEmployeeById: id={}, version={}, user={}", id, version, user.getUsername());

            final var employeeDb = validateDeletePerson(id, version, user);
            deleteAssociatedContacts(employeeDb);

            logger().debug("deleteEmployeeById: angestellter wurde gelöscht von {}", user.getUsername());

            Span keycloakSpan = tracer.spanBuilder("keycloak.delete").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.delete(user.getToken(), employeeDb.getUsername());
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler bei Keycloak-Delete");
                throw e;
            } finally {
                keycloakSpan.end();
            }

            Span mongoSpan = tracer.spanBuilder("person-repository.delete-employee").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                personRepository.delete(employeeDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim Löschen");
                throw e;
            } finally {
                mongoSpan.end();
            }

        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            serviceSpan.end();
        }
    }


    @Observed(name = "person-service.add-contact")
    public UUID addContact(final UUID customerId, final Contact contactInput, CustomUserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.add-contact").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("addContact: customerId={}, contactInput={}, user={}", customerId, contactInput, user.getUsername());

            final var customerDb = personReadService.findById(customerId, user);
            validateUserAccess(user, customerDb);

            if (customerDb.getCustomer().getContactIds() == null) {
                customerDb.getCustomer().setContactIds(new ArrayList<>());
            }

            final var existingContacts = customerDb.getCustomer().getContactIds().stream()
                .map(contactRepository::findById)  // `Optional<Contact>` wird erzeugt
                .flatMap(Optional::stream)         // Entfernt `Optional.empty()`
                .collect(Collectors.toList());      // Liste von `Contact` Objekten

            logger().debug("addContact: contacts={}, user={}", existingContacts, user.getUsername());

            validationService.validateContact(contactInput, existingContacts);
            contactInput.setId(UUID.randomUUID());
            final var contactDb = contactRepository.save(contactInput);

            logger().debug("addContact: contactDb={}, user={}", contactDb, user.getUsername());

            customerDb.getCustomer().getContactIds().add(contactDb.getId());

            Span mongoSpan = tracer.spanBuilder("person-repository.save").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                personRepository.save(customerDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim Speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }


            return contactDb.getId();
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            serviceSpan.end();
        }
    }

    @Observed(name = "person-service.update-contact")
    public Contact updateContact(final UUID customerId, final int customerVersion,  final UUID contactId, final int contactVersion, final Contact contactInput, final CustomUserDetails user) {
        Span span = tracer.spanBuilder("person-service.write.update-contact").startSpan();
        try (Scope scope = span.makeCurrent()) {
            assert scope != null;

            logger().debug("updateContact: customerId={},customerVersion={}, contactId={}, contactVersion={}, contactInput={}, user={}", customerId, customerVersion, contactId, contactVersion, contactInput, user.getUsername());

            final var customerDb = personReadService.findById(customerId, user);
            validateUserAccess(user, customerDb);

            if (!customerDb.getCustomer().getContactIds().contains(contactId)) {
                throw new NotFoundException(contactId);
            }

            final var contactDb = contactRepository.findById(contactId).orElseThrow(() -> new NotFoundException(contactId));
            validationService.validateContact(contactInput, contactDb, contactId);
            validationService.validateVersion(contactVersion, contactDb);
            contactDb.set(contactInput);
            contactRepository.save(contactDb);

            logger().debug("updateContact: contactDb={}, user={}", contactDb, user.getUsername());

            Span mongoSpan = tracer.spanBuilder("person-repository.save").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                personRepository.save(customerDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim Speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            return contactDb;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }


    @Observed(name = "person-service.remove-contact")
    public boolean removeContact(final UUID customerId, final int customerVersion,  final UUID contactId, final int contactVersion, final CustomUserDetails user) {
        Span span = tracer.spanBuilder("person-service.write.remove-contact").startSpan();
        try (Scope scope = span.makeCurrent()) {
            assert scope != null;

            logger().debug("removeContact: customerId={},customerVersion={}, contactId={}, contactVersion={}, user={}", customerId, customerVersion, contactId, contactVersion, user.getUsername());

            final var customerDb = personReadService.findById(customerId, user);
            validateUserAccess(user, customerDb);

            if (!customerDb.getCustomer().getContactIds().contains(contactId)) {
                throw new NotFoundException(contactId);
            }

            final var contactDb = contactRepository.findById(contactId).orElseThrow(() -> new NotFoundException(contactId));
            validationService.validateVersion(contactVersion, contactDb);
            validationService.validateVersion(customerVersion, customerDb);
            contactRepository.deleteById(contactId);

            customerDb.getCustomer().getContactIds()
                .stream()
                .filter(contactIdDb -> contactIdDb.equals(contactId))
                .toList()
                .forEach(contactIdDb -> customerDb.getCustomer().getContactIds().remove(contactIdDb));

            Span mongoSpan = tracer.spanBuilder("person-repository.save").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                personRepository.save(customerDb);
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setStatus(StatusCode.ERROR, "Fehler beim Speichern");
                throw e;
            } finally {
                mongoSpan.end();
            }

            logger().debug("removeContact: customerDb={}, user={}", customerDb, user.getUsername());
            return true;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }

    @Observed(name = "person-service.update-password")
    public void updatePassword(String newPassword, CustomUserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-service.write.update-password").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;

            logger().debug("updatePassword: newPassword={}, user={}", newPassword, user.getUsername());

            if (!checkPassword(newPassword)) {
                throw new PasswordInvalidException(newPassword);
            }

            Span keycloakSpan = tracer.spanBuilder("keycloak.update-password").startSpan();
            try (Scope keycloakScope = keycloakSpan.makeCurrent()) {
                assert keycloakScope != null;
                keycloakService.updatePassword(newPassword, user.getJwt());
            } catch (Exception e) {
                keycloakSpan.recordException(e);
                keycloakSpan.setStatus(StatusCode.ERROR, "Fehler beim Passwort-Update");
                throw e;
            } finally {
                keycloakSpan.end();
            }

        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setStatus(StatusCode.ERROR, "Fehler bei updatePassword");
            throw e;
        } finally {
            serviceSpan.end();
        }
    }




    private void validateNewPerson(final Person person, final String password, PersonType type) {
        if (personRepository.existsByEmail(person.getEmail()))
            throw new EmailExistsException(person.getEmail());

        var username = person.getUsername().toLowerCase(GERMAN);
        person.setUsername(username);

        if (personRepository.existsByUsername(username))
            throw new UsernameExistsException(username);

        if (!checkPassword(password))
            throw new PasswordInvalidException("Passwort erfüllt nicht die Sicherheitsrichtlinien");

        person.setId(UUID.randomUUID());
        logger().debug("{} {} wird erstellt mit ID={}", type, username, person.getId());
    }

    private Person validateUpdatePerson(UUID id, int version, CustomUserDetails user, PersonType type) {
        var dbPerson = personRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
        validationService.validateVersion(version, dbPerson);
        validateUserAccess(user, dbPerson);
        return dbPerson;
    }

    private Person validateDeletePerson(UUID id, int version, CustomUserDetails user) {
        var dbPerson = personRepository.findById(id).orElseThrow(NotFoundException::new);
        validationService.validateVersion(version, dbPerson);
        if (!isAdmin(user))
            throw new AccessForbiddenException(user.getUsername(), extractRoles(user));
        return dbPerson;
    }

    private void deleteAssociatedContacts(Person customerDb) {
        if (customerDb.getCustomer().getContactIds() == null) return;
        customerDb.getCustomer().getContactIds().stream()
            .flatMap(id -> contactRepository.findById(id).stream())
            .forEach(contactRepository::delete);
    }

    private void validateAndUpdateUserDetails(Person dbPerson, Person newPerson, CustomUserDetails user) {
        if (newPerson.getEmail() != null && !dbPerson.getEmail().equals(newPerson.getEmail()) &&
            personRepository.existsByEmail(newPerson.getEmail()))
            throw new EmailExistsException(newPerson.getEmail());

        if (newPerson.getUsername() != null && !dbPerson.getUsername().equalsIgnoreCase(newPerson.getUsername()) &&
            personRepository.existsByUsername(newPerson.getUsername()))
            throw new UsernameExistsException(newPerson.getUsername());

        newPerson.setUsername(newPerson.getUsername().toLowerCase(GERMAN));
    }

    private boolean isAdmin(CustomUserDetails user) {
        return extractRoles(user).contains(ADMIN);
    }

    private Set<RoleType> extractRoles(CustomUserDetails user) {
        return user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(auth -> RoleType.valueOf(auth.substring(RoleType.ROLE_PREFIX.length())))
            .collect(Collectors.toSet());
    }

    private void validateUserAccess(CustomUserDetails user, Person dbPerson) {
        if (!isAdmin(user) && !dbPerson.getUsername().equals(user.getUsername()))
            throw new AccessForbiddenException(user.getUsername(), extractRoles(user));
    }

    private String determineCustomerRole(int tierLevel) {
        return switch (tierLevel) {
            case 1 -> "Basic";
            case 2 -> "Elite";
            case 3 -> "Supreme";
            default -> throw new IllegalArgumentException("Ungültige Kundenstufe: " + tierLevel);
        };
    }

    private boolean checkPassword(final CharSequence password) {
        return password.length() >= MIN_LENGTH &&
            UPPERCASE.matcher(password).matches() &&
            LOWERCASE.matcher(password).matches() &&
            NUMBERS.matcher(password).matches() &&
            SYMBOLS.matcher(password).matches();
    }
}
