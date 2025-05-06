//package com.gentlecorp.person.config;
//
//import com.gentlecorp.person.models.entities.Person;
//import com.gentlecorp.person.models.entities.Address;
//import com.gentlecorp.person.models.entities.Customer;
//import com.gentlecorp.person.models.enums.GenderType;
//import com.gentlecorp.person.models.enums.MaritalStatusType;
//import com.gentlecorp.person.models.enums.StatusType;
//import com.gentlecorp.person.repositories.PersonRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@Profile("dev") // Nur im dev-Profil ausführen!
//public class DataSeeder implements CommandLineRunner {
//
//    private final PersonRepository personRepository;
//
//    @Override
//    public void run(String... args) {
//        log.info("🧹 Leere die Person-Datenbank...");
//        //personRepository.deleteAll();
//
//        log.info("🌱 Füge Beispielpersonen hinzu...");
//
//        Person demo = Person.builder()
//            .id(UUID.randomUUID())
//            .firstName("Max")
//            .lastName("Mustermann")
//            .email("max@example.com")
//            .username("max.muster")
//            .phoneNumber("+49123456789")
//            .birthdate(LocalDate.of(1990, 1, 1))
//            .gender(GenderType.MALE)
//            .personType(com.gentlecorp.person.models.enums.PersonType.CUSTOMER)
//            .address(Address.builder()
//                .street("Musterstraße")
//                .houseNumber("42")
//                .zipCode("12345")
//                .city("Musterstadt")
//                .country("Deutschland")
//                .build())
//            .customer(Customer.builder()
//                .tierLevel(1)
//                .subscribed(true)
//                .maritalStatus(MaritalStatusType.SINGLE)
//                .customerState(StatusType.ACTIVE)
//                .build())
//            .build();
//
//        personRepository.save(demo);
//
//        log.info("✅ Beispielperson gespeichert: {}", demo);
//    }
//}
