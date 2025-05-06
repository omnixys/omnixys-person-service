//package com.gentlecorp.person.init;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gentlecorp.person.models.entities.Contact;
//import com.gentlecorp.person.models.entities.Person;
//import com.gentlecorp.person.repositories.ContactRepository;
//import com.gentlecorp.person.repositories.PersonRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import java.io.InputStream;
//import java.util.List;
//
///**
// * Initialisiert die Datenbank beim Start mit Beispieldaten aus JSON-Dateien.
// * Bestehende Daten werden vorher gelöscht.
// * <p>
// * Die Dateien befinden sich im Ordner <code>src/main/resources/data/</code>.
// * </p>
// *
// * @author Rachel
// * @since 2025-04-18
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements ApplicationRunner {
//
//    private final PersonRepository personRepository;
//    private final ContactRepository contactRepository;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        log.info("\uD83D\uDEE0️ Dateninitialisierung gestartet...");
//
//        // 1. Alle bestehenden Daten löschen
//        contactRepository.deleteAll();
//        personRepository.deleteAll();
//
//        // 2. JSON-Dateien laden
//        InputStream customerStream = new ClassPathResource("data/customer.json").getInputStream();
//        InputStream contactStream = new ClassPathResource("data/contact.json").getInputStream();
//
//        // 3. JSON parsen
//        List<Person> customers = objectMapper.readValue(customerStream, new TypeReference<>() {});
//        List<Contact> contacts = objectMapper.readValue(contactStream, new TypeReference<>() {});
//
//        // 4. Daten speichern
//        personRepository.saveAll(customers);
//        contactRepository.saveAll(contacts);
//
//        log.info("✅ Datenbank erfolgreich mit {} Kunden und {} Kontakten befüllt.",
//            customers.size(), contacts.size());
//    }
//}
