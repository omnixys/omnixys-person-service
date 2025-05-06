package com.gentlecorp.person.utils;

import com.gentlecorp.person.exceptions.ConstraintViolationsException;
import com.gentlecorp.person.exceptions.ContactExistsException;
import com.gentlecorp.person.exceptions.VersionAheadException;
import com.gentlecorp.person.exceptions.VersionInvalidException;
import com.gentlecorp.person.exceptions.VersionOutdatedException;
import com.gentlecorp.person.messaging.KafkaPublisherService;
import com.gentlecorp.person.models.dto.ContactDTO;
import com.gentlecorp.person.models.entities.Contact;
import com.gentlecorp.person.models.entities.Person;
import com.gentlecorp.person.models.inputs.CreateCustomerInput;
import com.gentlecorp.person.tracing.LoggerPlus;
import com.gentlecorp.person.tracing.LoggerPlusFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.gentlecorp.person.utils.Constants.VERSION_NUMBER_MISSING;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;

@Service
@RequiredArgsConstructor
public class ValidationService {

  private final Validator validator;
  private final KafkaPublisherService kafkaPublisherService;
  private final LoggerPlusFactory factory;
  private LoggerPlus logger() {
    return factory.getLogger(getClass());
  }

  public <T> void validateDTO(T dto, Class<?>... groups) {
    // Standard-Validierung ausführen
    final Set<ConstraintViolation<T>> violations = validator.validate(dto, groups);

    // 🔥 Hier wird auch klassenbezogene Validierung berücksichtigt!
    final Set<ConstraintViolation<T>> classLevelViolations = validator.validate(dto);

    // Beide Validierungen zusammenführen
    violations.addAll(classLevelViolations);

    if (!violations.isEmpty()) {
      logger().debug("🚨 Validation failed: {}", violations);

      if (dto instanceof CreateCustomerInput) {
        @SuppressWarnings("unchecked")
        var customerViolations = new ArrayList<>((Collection<ConstraintViolation<CreateCustomerInput>>) (Collection<?>) violations);
        throw new ConstraintViolationsException(customerViolations, null);
      }

      if (dto instanceof ContactDTO) {
        @SuppressWarnings("unchecked")
        var contactViolations = new ArrayList<>((Collection<ConstraintViolation<ContactDTO>>) (Collection<?>) violations);
        throw new ConstraintViolationsException(null, contactViolations);
      }
    }
  }


  public  void validateContact (Contact newContact, List< Contact > contacts){
    contacts.forEach(
        contact -> {
          if (contact.getLastName().equals(newContact.getLastName()) && contact.getFirstName().equals(newContact.getFirstName())) {
            throw new ContactExistsException(contact.getLastName(), contact.getFirstName());
          }
        });
  }

  public void validateContact (Contact newContact, Contact existingContact,final UUID contactId){
    if (existingContact == null) {
      return;
    }

    if (existingContact.getId().equals(contactId)) {
      logger().error("Contact with id {} already exists", contactId);
      return;
    }

    if (existingContact.getFirstName().equals(newContact.getFirstName()) && existingContact.getLastName().equals(newContact.getLastName())) {
      logger().error("Contact with name {} already exists", newContact.getFirstName());
      throw new ContactExistsException(newContact.getLastName(), newContact.getFirstName());
    }
  }
  
  public int getVersion(final Optional<String> versionOpt, final HttpServletRequest request) {
    logger().trace("getVersion: {}", versionOpt);
    return versionOpt.map(versionStr -> {
      if (isValidVersion(versionStr)) {
        return Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
      } else {
        throw new VersionInvalidException(
          PRECONDITION_FAILED,
          String.format("Invalid ETag %s", versionStr), // Korrektur der String-Interpolation
          URI.create(request.getRequestURL().toString())
        );
      }
    }).orElseThrow(() -> new VersionInvalidException(
      PRECONDITION_REQUIRED,
      VERSION_NUMBER_MISSING,
      URI.create(request.getRequestURL().toString())
    ));
  }

  private boolean isValidVersion(String versionStr) {
    logger().debug("length of versionString={} versionString={}", versionStr.length(), versionStr);
    return versionStr.length() >= 3 &&
      versionStr.charAt(0) == '"' &&
      versionStr.charAt(versionStr.length() - 1) == '"';
  }

  public void validateVersion(int version, Person entity) {
    
    if (version < entity.getVersion()) {
      logger().error("Version is outdated");
      throw new VersionOutdatedException(version);
    }
    if (version > entity.getVersion()) {
      logger().error("Version is ahead of the current version");
      throw new VersionAheadException(version);
    }
  }

  public void validateVersion(int version, Contact entity) {
    
    if (version < entity.getVersion()) {
      logger().error("Version is outdated");
      throw new VersionOutdatedException(version);
    }
    if (version > entity.getVersion()) {
      logger().error("Version is ahead of the current version");
      throw new VersionAheadException(version);
    }
  }
}
