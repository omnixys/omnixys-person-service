package com.gentlecorp.person.messaging;

/**
 * Zentrale Konfiguration der Kafka-Topic-Namen.
 * <p>
 * Die Namen folgen dem Schema: {@code <service>.<entity>.<events>}.
 * </p>
 *
 * @author Caleb
 * @since 20.04.2025
 */
public final class KafkaTopicProperties {

    private KafkaTopicProperties() {
        // Utility class – private Konstruktor verhindert Instanziierung
    }

    /** ✉️ Mailversand bei Kundenregistrierung */
    public static final String TOPIC_NOTIFICATION_CUSTOMER_CREATED = "notification.customer.created";
    public static final String TOPIC_NOTIFICATION_CUSTOMER_DELETED = "notification.customer.deleted";

    /** 🏦 Kontoerstellung für neue Kunden */
    public static final String TOPIC_ACCOUNT_CREATED = "account.customer.created";

    public static final String TOPIC_ACCOUNT_DELETE = "account.customer.deleted";

    /** 🛒 Warenkorb anlegen */
    public static final String TOPIC_SHOPPING_CART_CREATED = "shopping-cart.customer.created";

    /** ❌ Warenkorb löschen */
    public static final String TOPIC_SHOPPING_CART_DELETED = "shopping-cart.customer.deleted";

    public static final String TOPIC_ACTIVITY_EVENTS = "activity.customer.log";

    public static final String TOPIC_SYSTEM_SHUTDOWN = "system.shutdown";
}
