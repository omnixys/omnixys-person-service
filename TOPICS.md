# 📬 Kafka Topics im Person-Service

Diese Datei dokumentiert alle Kafka-Topics, die vom Person-Service **verwendet oder veröffentlicht** werden.  
Alle Topics folgen dem Namensschema:

```
<service>.<entity>.<event>
```

## 🔁 Strukturübersicht

| Topic                             | Beschreibung                                              | Publisher                     | Empfänger                       | Event-Payload             |
|----------------------------------|-----------------------------------------------------------|-------------------------------|----------------------------------|---------------------------|
| `notification.customer.created`  | Versendet Willkommens-E-Mails an neue Kunden             | `KafkaPublisherService`       | `Notification-Service`           | `SendMailEvent`          |
| `account.customer.created`       | Erstellt ein Konto mit Startparametern                    | `KafkaPublisherService`       | `Account-Service`                | `AccountDTO`             |
| `shopping-cart.customer.created` | Erstellt einen Warenkorb für den neuen Benutzer           | `KafkaPublisherService`       | `ShoppingCart-Service`           | `ShoppingCartDTO`        |
| `shopping-cart.customer.deleted` | Löscht den Warenkorb bei Löschung des Benutzers          | `KafkaPublisherService`       | `ShoppingCart-Service`           | `UUID` als String        |

---

## 📦 Namenskonvention

- `<service>` = Ursprungsservice (z. B. `notification`, `account`, `shopping-cart`)
- `<entity>` = Betroffene Domänenentität (z. B. `customer`)
- `<event>` = Ereignistyp in Vergangenheitsform (`created`, `deleted`, etc.)

---

## 📞 Beispiel: Event-Payloads

### 🔹 SendMailEvent (`notification.customer.created`)
```json
{
  "email": "example@mail.com",
  "firstName": "Max",
  "lastName": "Mustermann",
  "role": "CUSTOMER"
}
```

### 🔹 AccountDTO (`account.customer.created`)
```json
{
  "balance": 0,
  "ibanCountryCode": "CH",
  "freeWithdrawals": 2,
  "withdrawalLimit": 50,
  "transferLimit": 20,
  "personId": "uuid"
}
```

### 🔹 ShoppingCartDTO (`shopping-cart.customer.created`)
```json
{
  "personId": "uuid",
  "username": "maxmuster",
  "notes": ""
}
```

### 🔹 UUID als String (`shopping-cart.customer.deleted`)
```json
"b7e243cf-3e28-4567-b30f-ec84fd419fff"
```

---

## 🔒 Sicherheit & Zugriff

| Topic                             | Lesezugriff (Consumer)             | Schreibzugriff (Producer)         |
|----------------------------------|------------------------------------|-----------------------------------|
| `notification.customer.created`  | Notification-Service               | Person-Service                    |
| `account.customer.created`       | Account-Service                    | Person-Service                    |
| `shopping-cart.customer.created` | ShoppingCart-Service               | Person-Service                    |
| `shopping-cart.customer.deleted` | ShoppingCart-Service               | Person-Service                    |

---

## 📚 Weitere Hinweise

- Topics sind **nicht dynamisch** und bleiben stabil (keine Benutzer-IDs im Topic-Namen!)
- Events können versioniert werden, z. B. `shopping-cart.customer.created.v1`
- DLQ- (Dead Letter Queue) Topics können als `*.dlt` angehängt werden
- ✔ EventWrapper-Unterstützung für alle Payloads vorhanden:

```java
EventWrapper<ShoppingCartDTO> wrapper = EventWrapper.of(
    shoppingCartDto,
    "shopping-cart.customer.created",
    "person-service",
    "v1"
);
kafkaTemplate.send("shopping-cart.customer.created", objectMapper.writeValueAsString(wrapper));
```

---

## 👷 Pflegehinweis

Wenn neue Events eingeführt werden, **bitte diese Datei mitdokumentieren** und ggf. Schema-Dateien ergänzen.





