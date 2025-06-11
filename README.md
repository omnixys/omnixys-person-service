# 👤 Omnixys Person Service

Der **Omnixys Person Service** ist ein Java-basierter Microservice zur Verwaltung von Personen innerhalb des modularen **OmnixysSphere**-Ökosystems. Er verarbeitet Kunden- und Mitarbeiterdaten, verwaltet Rollen, Kontakte und bietet vollständige Integration in das Authentifizierungs- und Tracingsystem.

---

## 🔍 Übersicht

* Verwaltung von **Personenprofilen**, inkl. Rollen & Kontakten
* Vollständig **GraphQL-basiert (Schema First)** mit Spring Boot
* Authentifizierung via **Keycloak**
* Messaging über **Apache Kafka**
* **Distributed Tracing** mit OpenTelemetry & Tempo
* Datenbank: MongoDB

---

## 🚀 Tech Stack

| Komponente        | Technologie                  |
| ----------------- | ---------------------------- |
| Sprache           | Java (24)                    |
| Framework         | Spring Boot                  |
| Authentifizierung | Keycloak                     |
| API               | GraphQL (Schema First)       |
| Messaging         | Apache Kafka                 |
| Tracing           | OpenTelemetry + Tempo        |
| Monitoring        | Prometheus + Grafana         |
| Datenbank         | MongoDB                      |
| Logging           | Kafka-basiert via LoggerPlus |
| Lizenz            | [GPLv3](./LICENSE.md)        |

---

## 📦 Features

* `createCustomer`, `createEmployee`, `updateContact`, `deletePerson` usw.
* Unterstützung für komplexe Rollenmodelle (Admin, Helper, etc.)
* Integrierte Tracing- und Loggingstruktur (TraceContext, LoggerPlus)
* Kafka Producer/Consumer für CRUD-Events

---

## 🧾 Projektstruktur

```bash
src/main/java/com/omnixys/person/
├── controller/
├── graphql/
│   ├── resolver/
│   ├── dto/
├── model/
├── repository/
├── service/
├── kafka/
├── tracing/
└── PersonApplication.java
```

---

## 🛠️ Starten (lokal)

```bash
# Projekt clonen
git clone https://github.com/omnixys/omnixys-person-service.git
cd omnixys-person-service

# Bauen
./gradlew build

# Starten
docker-compose up
```

---

## 🔐 Security & Tracing

* Alle Endpunkte sind mit **Keycloak (JWT)** gesichert
* Jeder API-Call erzeugt automatisch einen **Trace** (Tempo)
* Kafka-Events enthalten Tracing-Metadaten (`x-trace-id`, `x-service`, ...)

---

## 📣 Kafka Topics

| Topic                          | Richtung | Beschreibung                                         |
|--------------------------------| -------- | ---------------------------------------------------- |
| `shopping-cart.create.person`  | Producer | Reagiert auf neue Warenkörbe mit Person              |
| `shopping-cart.delete.person`  | Producer | Reagiert auf Warenkorb-Löschungen                    |
| `account.create.person`        | Producer | Wird beim Erstellen einer Person für Account erzeugt |
| `notification.create.person`   | Producer | Sendet Benachrichtigung bei Erstellung               |
| `notification.delete.person`   | Producer | Sendet Benachrichtigung bei Löschung                 |
| `person.shutdown.orchestrator` | Consumer | Wird konsumiert bei Core-Shutdown                    |
| `person.start.orchestrator`    | Consumer | Initialisiert Personenservice beim Start             |
| `person.restart.orchestrator`  | Consumer | Führt Re-Init des Service aus                        |
| `all.shutdown.orchestrator`    | Consumer | Wird konsumiert bei Core-Shutdown                    |
| `all.start.orchestrator`       | Consumer | Initialisiert Personenservice beim Start             |
| `all.restart.orchestrator`     | Consumer | Führt Re-Init des Service aus                        |
---

## 🧑‍💻 Contributing

Bitte lies [CONTRIBUTING.md](./CONTRIBUTING.md), bevor du Änderungen einreichst.
Wir verwenden Branch-Schemata wie `feature/<feature-name>` und PR-Templates mit Testhinweisen.

---

## 📄 Lizenz

Veröffentlicht unter der [GNU GPLv3](./LICENSE.md)
© 2025 [Omnixys](https://omnixys.com) – Modular Thinking. Infinite Possibilities.

---

## 🔍 `PersonQueryResolver` (lesend)

| Methode | Zweck |
|--------|-------|
| `getCustomerById(UUID id)` | Einzelnen Kunden abfragen |
| `getEmployeeById(UUID id)` | Einzelnen Mitarbeiter abfragen |
| `findCustomers(CustomerFilter filter)` | Kundenliste mit Filter (z. B. Status, Interesse, etc.) |
| `findEmployees(EmployeeFilter filter)` | Mitarbeiterliste mit Filter (z. B. Abteilung, Rolle, Position) |

---

## ✍️ `PersonMutationResolver` (schreibend)

| Methode | Zweck |
|--------|-------|
| `createCustomer(CreateCustomerInput)` | Erstellt neuen Kunden inkl. `Person` |
| `createEmployee(CreateEmployeeInput)` | Erstellt neuen Mitarbeiter inkl. `Person` |
| `updateCustomer(UUID id, UpdateCustomerInput)` | Aktualisiert Kundendaten |
| `updateEmployee(UUID id, UpdateEmployeeInput)` | Aktualisiert Mitarbeiterdaten |
| `addContact(UUID personId, ContactInput)` | Fügt Kontakt hinzu |
| `updateContact(UUID contactId, UpdateContactInput)` | Aktualisiert Kontakt |
| `removeContact(UUID contactId)` | Löscht Kontakt |
| `updatePassword(UUID personId, String newPassword)` | Passwort aktualisieren |
| `deleteCustomer(UUID personId)` | Kunde löschen |
| `deleteEmployee(UUID personId)` | Mitarbeiter löschen |
| ✅ Weitere Methoden | z. B. zum Zurücksetzen, Reaktivieren, Sperren etc.

