package com.gentlecorp.person.models.entities;

import com.gentlecorp.person.models.enums.GenderType;
import com.gentlecorp.person.models.enums.PersonType;
import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repräsentiert eine allgemeine Person, die entweder ein Kunde oder ein Mitarbeiter sein kann.
 * <p>
 * Diese Entität wird in MongoDB in der Collection 'Person' gespeichert.
 * Die Unterklassen `Customer` und `Employee` erben von `Person`.
 * </p>
 *
 * @since 26.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Document(collection = "persons")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    /**
     * Eindeutige ID des Kunden.
     * Automatische UUID-Generierung bei der Erstellung.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Versionsnummer für die Optimistic Locking-Strategie.
     */
    @Version
    private int version;

    /**
     * Nachname des Kunden (Pflichtfeld).
     */
    private String lastName;

    /**
     * Vorname des Kunden (Pflichtfeld).
     */
    private String firstName;

    /**
     * Typ der Person (Kunde oder Mitarbeiter).
     */
    private PersonType personType;

    /**
     * E-Mail-Adresse des Kunden (einzigartig und validiert).
     */
    @Indexed(unique = true)
    private String email;

    /**
     * Telefonnummer des Kunden (Pflichtfeld).
     */
    private String phoneNumber;

    /**
     * Benutzername des Kunden (einzigartig und validiert).
     */
    @Indexed(unique = true)
    private String username;

    /**
     * Geburtsdatum des Kunden (muss in der Vergangenheit liegen).
     */
    private LocalDate birthdate;

    /**
     * Geschlecht des Kunden.
     */
    private GenderType gender;

    /**
     * Adresse des Kunden (Pflichtfeld).
     */
    private Address address;

    private Customer customer;
    private Employee employee;

    /**
     * Zeitstempel der Erstellung des Kunden-Dokuments.
     */
    @CreatedDate
    private LocalDateTime created;

    /**
     * Zeitstempel der letzten Änderung des Kunden-Dokuments.
     */
    @LastModifiedDate
    private LocalDateTime updated;

    public void set(final Person person) {
        lastName = person.getLastName() != null ? person.getLastName() : lastName;
        firstName = person.getFirstName() != null ? person.getFirstName() : firstName;
        email = person.getEmail() != null ? person.getEmail() : email;
        phoneNumber = person.getPhoneNumber() != null ? person.getPhoneNumber() : phoneNumber;
        username = person.getUsername() != null ? person.getUsername() : username;
        birthdate = person.getBirthdate() != null ? person.getBirthdate() : birthdate;
        gender = person.getGender() != null ? person.getGender() : gender;
        address = person.getAddress() != null ? person.getAddress() : address;
        employee = person.getEmployee() != null ? person.getEmployee() : employee;
        customer = person.getCustomer() != null ? person.getCustomer() : customer;
    }
}
