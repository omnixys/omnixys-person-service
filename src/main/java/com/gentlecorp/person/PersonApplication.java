package com.gentlecorp.person;

import com.gentlecorp.person.config.AppProperties;
import com.gentlecorp.person.config.ApplicationConfig;
import com.gentlecorp.person.dev.DevConfig;
import com.gentlecorp.person.security.KeycloakProps;
import com.gentlecorp.person.utils.Env;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.data.federation.FederationSchemaFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static com.gentlecorp.person.utils.Banner.TEXT;


@SpringBootApplication(proxyBeanMethods = false)
@Import({ApplicationConfig.class, DevConfig.class})
@EnableConfigurationProperties({KeycloakProps.class, AppProperties.class})
@EnableWebSecurity
@EnableMethodSecurity
@EnableAsync
@SuppressWarnings({"ClassUnconnectedToPackage"})
public class PersonApplication{

    public static void main(String[] args) {
        new Env();
        final var app = new SpringApplication(PersonApplication.class);
        app.setBanner((_, _, out) -> out.println(TEXT));
        app.run(args);
    }

    @Bean
    public GraphQlSourceBuilderCustomizer graphQlSourceCustomizer(FederationSchemaFactory factory) {
        return builder -> builder.schemaFactory(factory::createGraphQLSchema);
    }

    @Bean
    FederationSchemaFactory federationSchemaFactory() {
        return new FederationSchemaFactory();
    }

    /**
     * Test-Trace beim Starten auslösen,
     * damit Tempo und Grafana direkt Traces empfangen.
     */
//    @Bean
//    ApplicationRunner startupTestTrace(ObservationRegistry registry) {
//        return args -> {
//            Observation.createNotStarted("startup-test-trace", registry)
//                .lowCardinalityKeyValue("startup", "true")
//                .observe(() -> {
//                    System.out.println("🌟 Startup-Trace erfolgreich ausgelöst!");
//                });
//        };
//    }
}