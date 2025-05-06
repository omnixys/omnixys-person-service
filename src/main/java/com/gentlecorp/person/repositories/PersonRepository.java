package com.gentlecorp.person.repositories;

import com.gentlecorp.person.models.entities.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository für den Zugriff auf Person-Dokumente in MongoDB.
 * <p>
 * Bietet CRUD-Funktionalität sowie erweiterbare Methoden zur Abfrage über UUID, E-Mail oder Username.
 * </p>
 *
 * @author Caleb Gyamfi
 * @since 26.02.2025
 */
@Repository
public interface PersonRepository extends MongoRepository<Person, UUID> {

    /**
     * Sucht eine Person anhand ihrer E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse.
     * @return Optional mit der gefundenen Person, falls vorhanden.
     */
    Optional<Person> findByEmail(String email);

    /**
     * Sucht eine Person anhand ihres Benutzernamens.
     *
     * @param username Der Benutzername.
     * @return Optional mit der gefundenen Person, falls vorhanden.
     */
    Optional<Person> findByUsername(String username);

    /**
     * Prüft, ob eine Person mit der gegebenen E-Mail existiert.
     *
     * @param email Die E-Mail-Adresse.
     * @return true, wenn eine Person mit dieser E-Mail existiert.
     */
    boolean existsByEmail(String email);

    /**
     * Prüft, ob eine Person mit dem gegebenen Benutzernamen existiert.
     *
     * @param username Der Benutzername.
     * @return true, wenn eine Person mit diesem Benutzernamen existiert.
     */
    boolean existsByUsername(String username);
}
