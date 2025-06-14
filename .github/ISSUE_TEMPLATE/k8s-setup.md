---
name: 🐳 Kubernetes Setup für Microservice
about: Erstelle alle K8s-Ressourcen (Deployment, Service, ConfigMap etc.) für einen Microservice
title: "[K8s] Deployment und Service für <service-name> in Kubernetes konfigurieren"
labels: ["type:infra", "kubernetes", "deployment", "priority:high"]
assignees: []
projects: []
milestone: "Phase 1 – Core Services"
---

## 🧩 Ziel

Der `<service-name>` (Technologie: <NestJS, FastAPI, Spring Boot>, Port `<port>`) soll in Kubernetes deploybar sein. Es soll ein vollständiger Satz an YAML-Dateien erstellt und getestet werden.

---

## ✅ Aufgaben

- [ ] Namespace `omnixys` anlegen (falls nicht vorhanden)
- [ ] `deployment.yaml` erstellen mit Container-Port `<container-port>` und Image `ghcr.io/omnixys/<service-name>:latest`
- [ ] `service.yaml` mit Port `<port>` (`type: LoadBalancer`)
- [ ] (optional) `configmap.yaml` für Keycloak-, DB-, oder andere ENV-Werte
- [ ] (optional) `ingress.yaml` für Domain-Zugriff `<service-name>.omnixys.local`
- [ ] Dateien im Ordner `k8s/<service-name>/` ablegen
- [ ] Anwendung mit `kubectl` deployen und Logs prüfen
- [ ] Zugriff testen

---

## 🔧 Technische Details

| Eigenschaft       | Wert                                 |
|------------------|--------------------------------------|
| Namespace         | `omnixys`                           |
| Image             | `ghcr.io/omnixys/<service-name>:latest` |
| Container-Port    | `<container-port>`                  |
| Service-Port      | `<port>`                            |

---

## 🏷 Labels

- `type:infra`
- `service:<service-name>`
- `kubernetes`
- `deployment`
- `priority:high`

---

## 📝 Hinweise

> Stelle sicher, dass die Ports laut [port-konvention.md](../port-konvention.md) vergeben werden und kein anderer Service im selben Namespace denselben Port verwendet.
