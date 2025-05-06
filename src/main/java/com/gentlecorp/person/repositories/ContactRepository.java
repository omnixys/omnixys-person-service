package com.gentlecorp.person.repositories;

import com.gentlecorp.person.models.entities.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository zur Verwaltung von {@link Contact}-Dokumenten in MongoDB.
 * <p>
 * Bietet CRUD-Funktionalität sowie individuelle Suchabfragen nach verknüpften Personen.
 * </p>
 *
 * @author Caleb
 * @since 13.02.2025
 */
@Repository
public interface ContactRepository extends MongoRepository<Contact, UUID> {

    /**
     * Sucht alle Kontakte mit einem bestimmten Nachnamen.
     *
     * @param lastName Nachname des Kontakts.
     * @return Liste aller Kontakte mit diesem Nachnamen.
     */
    List<Contact> findByLastName(String lastName);

    // Optional: Weitere Methoden bei Bedarf
    // z. B. findByFirstName, findByRelationship etc.
}
