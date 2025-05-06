package com.gentlecorp.person.tracing;

import com.gentlecorp.person.messaging.KafkaPublisherService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Erweiterter Logger mit Kafka-Unterstützung.
 * <p>
 * Loggt gleichzeitig in die Konsole (Slf4j) und an einen zentralen Logging-Service via Kafka.
 * </p>
 */
@RequiredArgsConstructor
public class LoggerPlus {

    private final Logger logger;
    private final String serviceName;
    private final KafkaPublisherService kafkaPublisherService;
    private final Class<?> clazz;

    public static LoggerPlus of(Class<?> clazz, final KafkaPublisherService publisher, final String serviceName) {
        return new LoggerPlus(LoggerFactory.getLogger(clazz), serviceName, publisher, clazz);
    }

    private String getCallerContext() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames
                .filter(f -> f.getDeclaringClass().equals(clazz))
                .findFirst()
                .map(frame -> String.format("%s#%s",clazz.getSimpleName(), frame.getMethodName()))
                .orElse(clazz.getSimpleName()));
    }

    private void sendLog(String level, String message) {
        var context = getCallerContext();
        kafkaPublisherService.log(level, message, serviceName, context);
    }

    public void debug(String format, Object... args) {
        String safeFormat = format != null ? format.replace("{}", "%s") : "";
        String msg = String.format(safeFormat, args != null ? args : new Object[]{});
        logger.debug(msg);
//        sendLog("DEBUG", msg);
    }

    public void info(String format, Object... args) {
        String safeFormat = format != null ? format.replace("{}", "%s") : "";
        String msg = String.format(safeFormat, args != null ? args : new Object[]{});
        logger.info(msg);
        sendLog("INFO", msg);
    }

    public void warn(String format, Object... args) {
        String safeFormat = format != null ? format.replace("{}", "%s") : "";
        String msg = String.format(safeFormat, args != null ? args : new Object[]{});
        logger.warn(msg);
        sendLog("WARN", msg);
    }

    public void error(String format, Object... args) {
        String safeFormat = format != null ? format.replace("{}", "%s") : "";
        String msg = String.format(safeFormat, args != null ? args : new Object[]{});
        logger.error(msg);
        sendLog("ERROR", msg);
    }

    public void trace(String format, Object... args) {
        String safeFormat = format != null ? format.replace("{}", "%s") : "";
        String msg = String.format(safeFormat, args != null ? args : new Object[]{});
        logger.trace(msg);
//        sendLog("TRACE", msg);
    }
}
