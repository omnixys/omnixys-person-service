package com.omnixys.person.services;

import com.omnixys.person.exceptions.AccessForbiddenException;
import com.omnixys.person.exceptions.NotFoundException;
import com.omnixys.person.models.entities.Contact;
import com.omnixys.person.models.entities.Person;
import com.omnixys.person.repositories.ContactRepository;
import com.omnixys.person.security.enums.RoleType;
import com.omnixys.person.tracing.LoggerPlus;
import com.omnixys.person.tracing.LoggerPlusFactory;
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

/**
 * Serviceklasse zur Verwaltung von Kundenleseoperationen.
 * <p>
 * Diese Klasse bietet Methoden zum Abrufen einzelner Kunden sowie zur Durchführung dynamischer Suchabfragen.
 * </p>
 *
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContactReadService {

    private final MongoTemplate mongoTemplate;
    private final Tracer tracer;
    private final ContactRepository contactRepository;
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
    @Observed(name = "contact-service.read.find-by-id")
    public @NonNull Contact findById(final UUID id, final UserDetails user) {
        Span serviceSpan = tracer.spanBuilder("contact-service.read.findById").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;
            logger().debug("findById: id={}", id);

            serviceSpan.setAttribute("user.id", user.getUsername());
            serviceSpan.setAttribute("contact.id", id.toString());

            Span mongoSpan = tracer.spanBuilder("mongo.findById").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                final var contact = contactRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(id));

                logger().debug("findById: contact={}", contact);
                return contact;
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
     * @param customerId Die Benutzer-ID für mandantenbasierte Filterung
     * @return Eine `Collection<Customer>` mit den gefundenen Kunden.
     */
    @Observed(name = "contact-service.read.find-by-customer-id")
    public @NonNull Collection<Contact> findByCustomerId(
        final UUID customerId
    ) {
        Span serviceSpan = tracer.spanBuilder("contact-service.read.find-by-customer-id").startSpan();
        try (Scope serviceScope = serviceSpan.makeCurrent()) {
            assert serviceScope != null;
            logger().debug("find: customerId={}", customerId);

            Span mongoSpan = tracer.spanBuilder("mongo.find.contacts").startSpan();
            try (Scope mongoScope = mongoSpan.makeCurrent()) {
                assert mongoScope != null;
                Query query = new Query().addCriteria(Criteria.where("customerId").is(customerId));
                serviceSpan.setAttribute("customer.id", customerId.toString());
                List<Contact> results = mongoTemplate.find(query, Contact.class);
                logger().info("find: {} contacts found for customer {}", results.size(), customerId);
                return results;
            } catch (Exception e) {
                mongoSpan.recordException(e);
                throw e;
            } finally {
                mongoSpan.end();
            }
        } catch (Exception e) {
            serviceSpan.recordException(e);
            throw e;
        } finally {
            serviceSpan.end();
        }
    }

}
