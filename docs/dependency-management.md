# 💶 Dependency Management in Omnixys Person Service

Dieses Dokument beschreibt, wie Abhängigkeiten im `omnixys-person-service` mit [Dependabot](https://docs.github.com/en/code-security/supply-chain-security/keeping-your-dependencies-updated-automatically/about-dependabot-version-updates) und GitHub Actions verwaltet werden.

---

## 🔧 Tools & Automatisierung

| Tool            | Zweck                                |
|-----------------|---------------------------------------|
| **Dependabot**  | Automatische Versionsupdates (Gradle, GitHub Actions) |
| **Auto-Assign** | Zuweisung von PRs an Maintainer       |
| **Auto-Merge**  | Automatischer Merge sicherer Updates (z. B. bei Patch-Level) |
| **Labels**      | Kategorisierung der PRs (`runtime`, `dev`, `github-actions`, `dependencies`) |

---

## 📋 Dependabot-Konfiguration

Die Datei befindet sich unter:

```
.github/dependabot.yml
```

### 🔁 Update-Zyklen

| Bereich             | Intervall   | Details                   |
|---------------------|-------------|---------------------------|
| GitHub Actions      | Wöchentlich | Sonntags, 04:00 Uhr       |
| Gradle Runtime Deps | Wöchentlich | Sonntags, 03:00 Uhr       |
| Gradle Dev Deps     | Monatlich   | Montags, 02:00 Uhr        |

### 🔒 Einschränkungen

- `spring-boot-starter-logging` wird ignoriert (≥ 3.0.0)
- Max. 5 gleichzeitige Runtime-PRs

---

## 🧠 Automatischer Review & Merge

Pull Requests von **Dependabot** werden:
- automatisch **Caleb-Script** zugewiesen
- mit Review durch `omnixys-maintainers` versehen
- **nur bei Label `automerge`** automatisch zusammengeführt, wenn:
    - ✅ alle Status-Checks bestanden
    - ✅ mind. ein Review vorhanden ist

> Siehe Workflow: `.github/workflows/auto-merge.yml`

---

## 🏷️ Label-Strategie

| Label           | Bedeutung                           |
|----------------|--------------------------------------|
| `dependencies` | Alle automatisierten Updates         |
| `runtime`      | Relevante Laufzeitabhängigkeiten     |
| `dev`          | Nur für Entwicklung/Test             |
| `github-actions` | GitHub Actions selbst               |
| `automerge`    | Wird für automatische Merges benötigt |

---

## 📤 Artifact-Speicher

- HTML-Reports aus Sicherheitschecks (Snyk, OWASP DC) werden als Artifacts gespeichert
- Speicherzeit: gemäß `upload-artifact@v4` (Standard: 90 Tage)

---

## 📬 Beteiligte Actions

- [`dependabot`](https://github.com/dependabot)
- [`peter-evans/enable-pull-request-automerge`](https://github.com/peter-evans/enable-pull-request-automerge)
- [`pozil/auto-assign-issue`](https://github.com/pozil/auto-assign-issue)

---

## ✋ Manuelles Labeln & Review

Wenn ein PR **nicht automatisch gemerged** wird:
1. Prüfe Label & Status
2. Reviewer-Approval geben
3. Merge manuell oder per `automerge`-Label aktivieren

---

© 2025 Omnixys – Modular Thinking. Infinite Possibilities.
