package com.omnixys.person.messaging;

import lombok.RequiredArgsConstructor;

/**
 * Zentrale Konfiguration der Kafka-Topic-Namen.
 * <p>
 * Die Namen folgen dem Schema: {@code <service>.<events>.<service>}.
 * </p>
 *
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @since 07.05.2025
 */
@RequiredArgsConstructor
public final class KafkaTopicProperties {

    public static final String TOPIC_NOTIFICATION_CREATE_PERSON = "notification.create.person";
    public static final String TOPIC_NOTIFICATION_DELETE_PERSON = "notification.delete.person";

    public static final String TOPIC_ACCOUNT_CREATE_PERSON = "account.create.person";
    public static final String TOPIC_ACCOUNT_DELETE_PERSON = "account.delete.person";


    public static final String TOPIC_SHOPPING_CART_CREATE_PERSON = "shopping-cart.create.person";
    public static final String TOPIC_SHOPPING_CART_DELETE_PERSON = "shopping-cart.delete.person";

    public static final String TOPIC_LOG_STREAM_LOG_PERSON = "log-Stream.log.person";

    public static final String TOPIC_PERSON_SHUTDOWN_ORCHESTRATOR = "person.shutdown.orchestrator";
    public static final String TOPIC_PERSON_START_ORCHESTRATOR = "person.start.orchestrator";
    public static final String TOPIC_PERSON_RESTART_ORCHESTRATOR = "person.restart.orchestrator";

    public static final String TOPIC_ALL_SHUTDOWN_ORCHESTRATOR = "all.shutdown.orchestrator";
    public static final String TOPIC_ALL_START_ORCHESTRATOR = "all.start.orchestrator";
    public static final String TOPIC_ALL_RESTART_ORCHESTRATOR = "all.restart.orchestrator";
}
