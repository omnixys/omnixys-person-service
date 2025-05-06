package com.gentlecorp.person.services;

import com.gentlecorp.person.exceptions.AccessForbiddenException;
import com.gentlecorp.person.exceptions.NotFoundException;
import com.gentlecorp.person.messaging.KafkaPublisherService;
import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.repositories.PersonRepository;
import com.gentlecorp.person.resolvers.PersonQueryResolver;
import com.gentlecorp.person.security.enums.RoleType;
import com.gentlecorp.person.tracing.LoggerPlus;
import com.gentlecorp.person.tracing.LoggerPlusFactory;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.gentlecorp.person.security.enums.RoleType.ADMIN;
import static com.gentlecorp.person.security.enums.RoleType.USER;

/**
 * Serviceklasse zur Verwaltung von Kundenleseoperationen.
 * <p>
 * Diese Klasse bietet Methoden zum Abrufen einzelner Kunden sowie zur Durchführung dynamischer Suchabfragen.
 * </p>
 *
 * @author Caleb Gyamfi
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PersonReadService {

    private final MongoTemplate mongoTemplate;
    private final Tracer tracer;
    private final PersonRepository personRepository;
    private final LoggerPlusFactory factory;
    private LoggerPlus logger() {
        return factory.getLogger(getClass());
    }

    /**
     * Findet einen Kunden anhand seiner ID.
     * <p>
     * Diese Methode überprüft, ob der angemeldete Benutzer Zugriff auf die angeforderte Kunden-ID hat.
     * Administratoren und Benutzer mit entsprechender Berechtigung dürfen alle Kunden einsehen.
     * </p>
     *
     * @param id   Die eindeutige Kunden-ID.
     * @param user Der angemeldete Benutzer, der die Abfrage durchführt.
     * @return Der gefundene Kunde.
     * @throws NotFoundException Falls kein Kunde mit der angegebenen ID gefunden wird.
     * @throws AccessForbiddenException Falls der Benutzer keinen Zugriff auf den Kunden hat.
     */
    @Observed(name = "person-service.read.find-by-id")
    public @NonNull Person findById(final UUID id, final UserDetails user) {
        Span serviceSpan = tracer.spanBuilder("person-service.read.findById").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;
            logger().debug("findById: id={}", id);

            serviceSpan.setAttribute("user.id", user.getUsername());
            serviceSpan.setAttribute("person.id", id.toString());

            Span mongoSpan = tracer.spanBuilder("mongo.findById").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                final var person = personRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id));

                if (person.getUsername().equals(user.getUsername())) {
                    logger().debug("findById: person={}", person);
                    return person;
                }

                final var roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(str -> str.substring(RoleType.ROLE_PREFIX.length()))
                    .map(RoleType::valueOf)
                    .toList();

                if (!roles.contains(ADMIN) && !roles.contains(USER)) {
                    throw new AccessForbiddenException(user.getUsername(), roles);
                }

                logger().debug("findById: person={}", person);
                return person;
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setAttribute("exception.class", e.getClass().getSimpleName());
                throw e;
            } finally {
                mongoSpan.end();
            }
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setAttribute("exception.class", e.getClass().getSimpleName());
            throw e;
        } finally {
            serviceSpan.end();
        }
    }

    /**
     * Führt eine dynamische Filter-, Paginierungs- und Sortierabfrage aus.
     * <p>
     * Diese Methode generiert eine MongoDB-Abfrage basierend auf den übergebenen Filterkriterien.
     * </p>
     *
     * @param filter Eine `Map<String, Object>` mit den Filterbedingungen.
     * @param page   Die gewünschte Seite (beginnend bei 0).
     * @param size   Die Anzahl der Einträge pro Seite.
     * @param sort   Eine `Map<String, String>` mit den Sortierkriterien (Feldname -> "ASC"/"DESC").
     * @return Eine `Collection<Customer>` mit den gefundenen Kunden.
     */
    @Observed(name = "person-service.read.find")
    public @NonNull Collection<Person> find(
        Map<String, Object> filter, int page, int size, Map<String, String> sort
    ) {
        Span serviceSpan = tracer.spanBuilder("person-service.read.find").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;
            logger().debug("find: filter={}, page={}, size={}, sort={}", filter, page, size, sort);

            serviceSpan.setAttribute("filter.count", filter.size());
            serviceSpan.setAttribute("page.number", page);
            serviceSpan.setAttribute("page.size", size);

            Span mongoSpan = tracer.spanBuilder("mongo.dynamicFind").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                List<Sort.Order> orders = sort.entrySet().stream()
                    .map(entry -> new Sort.Order(
                        "ASC".equalsIgnoreCase(entry.getValue()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                        entry.getKey()
                    ))
                    .toList();

                Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
                Query query = new Query();
                if (!filter.isEmpty()) {
                    query.addCriteria(new Criteria().andOperator(
                        filter.entrySet().stream()
                            .map(e -> Criteria.where(e.getKey()).is(e.getValue()))
                            .toArray(Criteria[]::new)
                    ));
                }
                query.with(pageable);

                final var people = mongoTemplate.find(query, Person.class);
                logger().debug("find: people={}", people);
                return people;
            } catch (Exception e) {
                mongoSpan.recordException(e);
                mongoSpan.setAttribute("exception.class", e.getClass().getSimpleName());
                throw e;
            } finally {
                mongoSpan.end();
            }
        } catch (Exception e) {
            serviceSpan.recordException(e);
            serviceSpan.setAttribute("exception.class", e.getClass().getSimpleName());
            throw e;
        } finally {
            serviceSpan.end();
        }
    }
}
