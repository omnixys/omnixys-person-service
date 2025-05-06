package com.gentlecorp.person.utils;

import java.util.regex.Pattern;

/**
 * Definiert allgemeine Konstanten für die Anwendung.
 * <p>
 * Enthält reguläre Ausdrücke, Validierungsregeln und andere wiederverwendbare Werte.
 * </p>
 *
 * @since 13.02.2025
 * @author <a href="mailto:caleb-script@outlook.de">Caleb Gyamfi</a>
 * @version 1.0
 */
public class Constants {
  /** Basis-URL für Problem-Details */
  public static final String PROBLEM_PATH = "/problem";
  /** Regulärer Ausdruck zur Überprüfung von UUIDs */
  public static final String ID_PATTERN = "[\\da-f]{8}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{4}-[\\da-f]{12}";
  /** Fehlermeldung für fehlende Versionsnummern */
  public static final String VERSION_NUMBER_MISSING = "Versionsnummer fehlt";

  /** Minimale Länge für Passwörter */
  public static final int MIN_LENGTH = 8;

  /** Reguläre Ausdrücke zur Passwortprüfung */
  public static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
  public static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
  public static final Pattern NUMBERS = Pattern.compile(".*\\d.*");
  @SuppressWarnings("RegExpRedundantEscape")
  public static final Pattern SYMBOLS = Pattern.compile(".*[!-/:-@\\[-`{-\\~].*");

  public interface OnCreate { }

  // Konstante für die minimale und maximale Mitgliedschaftsstufe.
  public static final long MIN_LEVEL = 1L;
  public static final long MAX_LEVEL = 3L;

  // Muster für gültige Namen (Nachname & Vorname).
  public static final String LAST_NAME_PATTERN = "(o'|von|von der|von und zu|van)?[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?";
  public static final String FIRST_NAME_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+(-[A-ZÄÖÜ][a-zäöüß]+)?";

  // Muster für gültige Benutzernamen.
  public static final String USERNAME_PATTERN = "[a-zA-Z0-9_\\-.]{4,}";

  // Zeichenbeschränkungen für verschiedene Eingaben.
  public static final int USERNAME_MAX_LENGTH = 20;
  public static final int USERNAME_MIN_LENGTH = 4;
  public static final int NAME_MAX_LENGTH = 40;
  public static final int EMAIL_MAX_LENGTH = 40;

  // Muster und Längenbeschränkungen für Telefonnummern.
  public static final String PHONE_NUMBER_PATTERN = "^\\+?[0-9. ()-]{7,25}$";
  public static final int PHONE_NUMBER_MAX_LENGTH = 25;
  public static final int PHONE_NUMBER_MIN_LENGTH = 7;
}
