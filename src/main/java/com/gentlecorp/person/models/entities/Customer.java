package com.gentlecorp.person.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gentlecorp.person.models.enums.*;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.UUID;

/**
 * Repräsentiert einen Kunden im System.
 * <p>
 * Diese Entität wird in der MongoDB in der Collection 'Customer' gespeichert.
 * </p>
 *
 * @since 13.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 2.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    /**
     * Stufe des Kunden im System (1–10).
     */
    @Min(value = 1, message = "Tier Level muss mindestens 1 sein")
    @Max(value = 10, message = "Tier Level darf maximal 10 sein")
    private int tierLevel;

    /**
     * Gibt an, ob der Kunde abonniert ist.
     */
    private boolean subscribed;

    /**
     * Familienstand des Kunden.
     */
    private MaritalStatusType maritalStatus;

    /**
     * Status des Kunden (z. B. aktiv, inaktiv).
     */
    private StatusType customerState;

    /**
     * Liste von Kontakten, die mit dem Kunden verknüpft sind.
     */
//    @Field("contacts") // für MongoDB-Mapping
//    @JsonProperty("contacts") // für Jackson-Deserialisierung
    private List<UUID> contactIds;

    /**
     * Liste der Interessen des Kunden.
     */
    private List<InterestType> interests;

    /**
     * Bevorzugte Kontaktoptionen des Kunden.
     */
    private List<ContactOptionsType> contactOptions;

//    public void set(final Customer customer) {
//        lastName = customer.getLastName() != null ? customer.getLastName() : lastName;
//        firstName = customer.getFirstName() != null ? customer.getFirstName() : firstName;
//        email = customer.getEmail() != null ? customer.getEmail() : email;
//        phoneNumber = customer.getPhoneNumber() != null ? customer.getPhoneNumber() : phoneNumber;
//        username = customer.getUsername() != null ? customer.getUsername() : username;
//
//        tierLevel = customer.getTierLevel() > 0 ? customer.getTierLevel() : tierLevel;
//        subscribed = customer.isSubscribed();
//
//        birthdate = customer.getBirthdate() != null ? customer.getBirthdate() : birthdate;
//        gender = customer.getGender() != null ? customer.getGender() : gender;
//        maritalStatus = customer.getMaritalStatus() != null ? customer.getMaritalStatus() : maritalStatus;
//        customerState = customer.getCustomerState() != null ? customer.getCustomerState() : customerState;
//
//        address = customer.getAddress() != null ? customer.getAddress() : address;
//        contactIds = customer.getContactIds() != null ? customer.getContactIds() : contactIds;
//
//        interests = customer.getInterests() != null ? customer.getInterests() : interests;
//        contactOptions = customer.getContactOptions() != null ? customer.getContactOptions() : contactOptions;
//
//    }
}
