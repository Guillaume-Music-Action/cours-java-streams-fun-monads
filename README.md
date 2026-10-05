# Cours : Streams Java 25 et programmation fonctionnelle

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Wrapper-02303A?logo=gradle&logoColor=white)
![TDD](https://img.shields.io/badge/methodology-TDD-ff69b4)
![Vavr](https://img.shields.io/badge/library-Vavr_0.10.6-blue)

Ce dépôt héberge les supports pratiques, exercices et implémentations du cours **Streams Java 25 et programmation fonctionnelle**.

Pour le plan détaillé du cours et l'analyse comparative JDK 25 vs Vavr, consultez [`cours-streams-java25-fp.md`](cours-streams-java25-fp.md).

---

## Philosophie et pédagogie : 100% TDD

Le cours repose intégralement sur une approche **Test-Driven Development (TDD)** :
- **Pas de cours magistral abstrait** : les concepts (foncteurs, monoïdes, monades, gatherers, immutabilité persistante) sont explorés et validés par l'écriture de tests.
- **Outillage pré-configuré** :
  - **JUnit Jupiter (JUnit 5)** pour orchestrer la suite de tests et structurer les scénarios (`@Nested`, `@DisplayName`, tests paramétrés).
  - **AssertJ** pour des assertions fluides, lisibles et expressives sur les collections, types optionnels et objets composites.
  - **Vavr** intégré comme boîte à outils fonctionnelle complémentaire au JDK.
- **Fil conducteur** :
  1. Partir de ce que le **JDK 25** propose nativement (`Stream`, `Gatherers` - JEP 485, lambdas, records, sealed interfaces, pattern matching).
  2. Éprouver et constater les limites du JDK en écrivant des tests ciblés.
  3. Reconstruire certaines abstractions par nous-mêmes en TDD (lois du foncteur, du monoïde et de la monade, types `Result` / `Either`, gatherers personnalisés).
  4. Comparer avec les solutions prêtes à l'emploi apportées par **Vavr** (`Try`, `Either`, `Validation`, collections persistantes, tuples, fonctions d'ordre supérieur jusqu'à l'arité 8).

---

## Stack technique

- **Java 25** (OpenJDK / Temurin toolchain)
- **Gradle 9** (Kotlin DSL)
- **JUnit Jupiter** 6.0.0 (via BOM)
- **AssertJ Core** 3.27.3
- **Vavr** 0.10.6

---

## Exécuter les tests

Lancer l'ensemble des tests via le Gradle Wrapper :
```sh
./gradlew test
```

Lancer un test spécifique :
```sh
./gradlew test --tests "org.example.ModernJupiterTest"
```

---

## Setup DevPod

Ce projet tourne dans DevPod (image `mcr.microsoft.com/devcontainers/java`, extensions Java pinnées pour compatibilité avec le langage server). Avant votre premier `devpod up` sur ce projet, exécutez une fois sur votre machine :

**macOS / Linux :**
```sh
./scripts/setup-devpod.sh
```

**Windows (PowerShell) :**
```powershell
.\scripts\setup-devpod.ps1
```

Ce script configure DevPod pour installer une version récente d'`openvscode-server` (au lieu de sa version par défaut, trop ancienne pour les extensions Java actuelles). C'est un réglage local à votre machine (`~/.devpod/config.yaml` sur macOS/Linux, `%USERPROFILE%\.devpod\config.yaml` sur Windows), pas un réglage du workspace — il doit être relancé une fois par machine, pas par workspace.
