package com.gentlecorp.person.security.service;

import com.gentlecorp.person.messaging.KafkaPublisherService;
import com.gentlecorp.person.resolvers.PersonQueryResolver;
import com.gentlecorp.person.security.KeycloakProps;
import com.gentlecorp.person.exceptions.NotFoundException;
import com.gentlecorp.person.exceptions.SignUpException;
import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.security.KeycloakRepository;
import com.gentlecorp.person.security.dto.TokenDTO;
import com.gentlecorp.person.security.dto.UserRepresentation;
import com.gentlecorp.person.tracing.LoggerPlus;
import com.gentlecorp.person.tracing.LoggerPlusFactory;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Service für die Integration mit Keycloak.
 * <p>
 * Dieser Service ermöglicht Benutzerregistrierung, Authentifizierung und Rollenverwaltung über Keycloak.
 * </p>
 *
 * @since 14.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class KeycloakService {

  private final KeycloakRepository keycloakRepository;
  private final KeycloakProps keycloakProps;
  private String clientAndSecretEncoded;
  private final LoggerPlusFactory factory;
  private LoggerPlus logger() {
    return factory.getLogger(getClass());
  }

  /**
   * Kodiert die Client-ID und das Client-Secret für die Authentifizierung mit Keycloak.
   */
  @PostConstruct
  private void encodeClientAndSecret() {
    final var clientAndSecret = keycloakProps.clientId() + ':' + keycloakProps.clientSecret();
    clientAndSecretEncoded = Base64
      .getEncoder()
      .encodeToString(clientAndSecret.getBytes(Charset.defaultCharset()));
  }

  /**
   * Registriert einen neuen Benutzer in Keycloak.
   *
   * @param customer Die Kundendaten.
   * @param password Das Passwort des Kunden.
   * @param role     Die zugewiesene Rolle.
   */
  @Observed(name = "keycloak-sign-in")
  public void signIn(final Person customer, final String password, final String role) {
    logger().debug("signIn: customer data prepared for registration");
    // logger().debug("signIn: customer={}", customer.getUsername());

    // JSON data for registration
    final var customerData = """
            {
                "username": "%s",
                "enabled": true,
                "firstName": "%s",
                "lastName": "%s",
                "email": "%s",
                "credentials": [{
                    "type": "password",
                    "value": "%s",
                    "temporary": false
                }]
            }
            """.formatted(
      customer.getUsername(),
      customer.getFirstName(),
      customer.getLastName(),
      customer.getEmail(),
      password // Ensure password is present in Customer object
    );

    //logger().debug("signIn: customerData={}", customerData);

    try {
      // Register user in Keycloak and get user ID
      keycloakRepository.signIn(
        customerData,
        "Bearer " + getAdminToken(),
        APPLICATION_JSON_VALUE
      );
      logger().info("signIn: Customer registered in Keycloak");

      final var accessToken = login(customer.getUsername(), password).access_token();
      final var userId = getUserInfo(accessToken);
      logger().debug("signIn: userId={}", userId);

      // Assign role to user
      assignRoleToUser(userId, role);

    } catch (Exception e) {
      logger().error("Error during user registration: ", e);
      throw new SignUpException("User registration failed: " + e.getMessage());
    }
  }

  @Observed(name = "keycloak-update")
  public void update(final Person customer, final Jwt jwt, final boolean isAdmin, final String oldUsername) {
    logger().debug("update: customer={} isAdmin={}", customer, isAdmin);

    // Retrieve user ID based on access token
    String userId;
    if (isAdmin)  {
      final var token = "Bearer " + jwt.getTokenValue();
      final var userList = keycloakRepository.getUserByUsername(token, oldUsername);
      userId = userList.stream()
        .map(UserRepresentation::id)
        .findFirst().orElseThrow(() -> new NotFoundException(customer.getUsername()));
    } else {
       userId = getUserID(jwt);
    }
    logger().debug("update: userId={}", userId);
    // JSON data for user update
    final var userData = """
          {
            "firstName": "%s",
            "lastName": "%s",
            "email": "%s",
            "username": "%s",
            "enabled": true
          }
          """.formatted(
      customer.getFirstName(),
      customer.getLastName(),
      customer.getEmail(),
      customer.getUsername()
    );
    logger().debug("update: userData={}", userData);

    final var adminToken = isAdmin
      ? jwt.getTokenValue()
      : getAdminToken();
    try {
      // Call repositories to update user in Keycloak
      keycloakRepository.updateUser(
        userData,
        "Bearer " + adminToken,
        APPLICATION_JSON_VALUE,
        userId
      );
    } catch (Exception e) {
      logger().error("Error updating user: ", e);
      throw new RuntimeException("Failed to update user: " + e.getMessage());
    }
  }

  @Observed(name = "keycloak-update-password")
  public void updatePassword(String newPassword, final Jwt jwt) {

    final var userId = getUserID(jwt);

    final var passwordData = """
          {
            "type": "password",
            "value": "%s",
            "temporary": false
          }
          """.formatted(newPassword);

    logger().debug("updatePassword: updating password for user with ID={}", userId);
    //  logger().debug("updatePassword: passwordData={}", passwordData);

    try {
      keycloakRepository.updateUserPassword(
        passwordData,
        "Bearer " + getAdminToken(),
        APPLICATION_JSON_VALUE,
        userId
      );
    } catch (Exception e) {
      logger().error("Error updating password for user {}: ", userId, e);
      throw new RuntimeException("Failed to update password for user: " + e.getMessage());
    }
  }

  @Observed(name = "keycloak-delete")
  public void delete(final String token, final String username) {
    logger().debug("delete: username={}", username);
    final var authToken = String.format("Bearer %s", token);
    final var userList = keycloakRepository.getUserByUsername(authToken, username);
    logger().debug("delete: users={}", userList);
    final var userId = userList.stream()
      .map(UserRepresentation::id)
      .findFirst().orElseThrow(() -> new NotFoundException(username));

    logger().debug("delete: userId={}", userId);
    keycloakRepository.deleteUser(authToken, userId);
  }

  /**
   * Extrahiert die Benutzer-ID aus dem JWT.
   *
   * @param jwt Das JWT-Token.
   * @return Die Benutzer-ID.
   * @throws NotFoundException Falls die Benutzer-ID nicht gefunden wird.
   */
  private String getUserID(final Jwt jwt) {
    logger().debug("getUserID");
    if (jwt == null) {
      throw new NotFoundException();
    }
    final var id = (String) jwt.getClaims().get("sub");
    logger().debug("getUserID: id={}", id);
    return id;
  }

  private void assignRoleToUser(String userId, String roleName) {
    logger().debug("Assigning role {} to user {}", roleName, userId);

    final var token = getAdminToken();
    final var roleId = getRole(roleName, token);

    // JSON data for role assignment
    final var roleData = """
            [{
                "id": "%s",
                "name": "%s"
            }]
            """.formatted(roleId, roleName);

    logger().debug("roleData={}", roleData);
    try {
      keycloakRepository.assignRoleToUser(
          roleData,
          "Bearer " + getAdminToken(),
          APPLICATION_JSON_VALUE,
          userId
      );

    } catch (Exception e) {
      logger().error("Error assigning role to user: ", e);
      throw new RuntimeException("Failed to assign role to user: " + e.getMessage());
    }
  }

  private String getRole(final String roleName, final String token) {
    logger().debug("getRole: roleName={}", roleName);
    // logger().debug("getRole: roleName={}, token={}", roleName, token);

    final var roles = keycloakRepository.getRoles("Bearer " + token, APPLICATION_JSON_VALUE);
    logger().debug("getRole: roles={}", roles);

    final var role = roles.stream()
        .filter(r -> r.name().equals(roleName)).findFirst().orElse(null);

    if (role == null) {
      throw new RuntimeException("RoleDTO not found: " + roleName);
    }
    logger().debug("getRole: role={}", role);
    return role.id();
  }

  /**
   * Meldet einen Benutzer mit Benutzername und Passwort bei Keycloak an.
   *
   * @param username Der Benutzername.
   * @param password Das Passwort.
   * @return Ein `TokenDTO`, das ein Zugriffstoken enthält.
   */
  private TokenDTO login(final String username, final String password) {
    return keycloakRepository.login(
        "grant_type=password&username=" + username
            + "&password=" + password
            + "&client_id=" + keycloakProps.clientId()
            + "&client_secret=" + keycloakProps.clientSecret()
            + "&scope=openid",
        "Basic " + clientAndSecretEncoded,
        APPLICATION_FORM_URLENCODED_VALUE
    );
  }

  /**
   * Ruft das Admin-Token aus Keycloak ab.
   *
   * @return Das Zugriffstoken für den Admin.
   */
  private String getAdminToken() {
    logger().debug("getAdminToken");
    final var adminToken = login("admin", "p");
    return adminToken.access_token();
  }

  private String getUserInfo(final String token) {
    // logger().debug("getUserInfo: token={}", token);

    final var info = keycloakRepository.userInfo("Bearer " + token, APPLICATION_FORM_URLENCODED_VALUE);
    return info.sub();
  }

}
