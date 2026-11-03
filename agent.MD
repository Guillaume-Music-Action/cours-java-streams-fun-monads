# agent.MD — Écrire des classes de test pédagogiques pour le cours

Ce fichier cadre le travail d'un agent (LLM) qui écrit des **classes de test servant d'illustration au cours** *Streams Java 25 et programmation fonctionnelle*.

> **Ce code n'est PAS du code de production.** Il sert à enseigner. Il doit être **court, lisible, commenté et fidèle au cours**. Ne pas le transformer en projet d'ingénierie.

---

## 1. Contexte

- Dépôt de cours, pédagogie **100 % TDD** : les concepts sont explorés *par des tests*. Voir `README.md` et `cours-streams-java25-fp.md` (plan du cours et vocabulaire à réutiliser).
- Stack : **Java 25** (toolchain Gradle), **JUnit Jupiter 6** (`@Nested`, `@DisplayName`, `@ParameterizedTest`), **AssertJ** (`assertThat`, `assertThatNullPointerException`…), **Vavr 0.10.6** (comparaison avec le JDK).
- Organisation : `src/test/java/org/example/moduleNN/`, une classe par notion, nommée `<Notion>Test.java` ; les variantes Vavr sont dans `<Notion>VavrTest.java`.
- Exemple de référence à imiter : `src/test/java/org/example/module01/FunctionTest.java` et `FunctionVavrTest.java`.

## 2. Objectif de chaque classe de test

Une classe = **une notion du cours**, illustrée par des tests qui se lisent comme des phrases :

1. Le test **montre** un comportement (ce que fait `andThen`, pourquoi une capture doit être *effectively final*, etc.).
2. Le test **affirme** ce comportement avec AssertJ, de façon lisible.
3. Le commentaire **explique le pourquoi**, en français, à un étudiant qui découvre le sujet.

Le test doit pouvoir être lu comme une démonstration au tableau.

## 3. Structure et style attendus

Reproduire le gabarit suivant :

```java
package org.example.moduleNN;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Module N - Notion")
class NotionTest {

    @Nested
    @DisplayName("sous-thème du cours")
    class SousTheme {

        @Test
        @DisplayName("phrase qui décrit le comportement observé")
        void comportementObserve() {
            // Commentaire pédagogique : ce qu'on veut faire remarquer et pourquoi.
            var resultat = ...;

            assertThat(resultat).isEqualTo(...);
        }
    }
}
```

Règles :

- **Un `@Nested` par sous-thème**, dans l'ordre pédagogique du cours (du plus simple au plus avancé).
- **`@DisplayName` en français** sur la classe, les `@Nested` et chaque test. Le nom du test est une phrase au présent décrivant ce qui est observé (« andThen applique d'abord le receveur »).
- **Un concept par test.** Plusieurs `assertThat` sont acceptables s'ils illustrent le *même* concept (ex. le test d'identité qui vérifie `andThen` *et* `compose`).
- **Assertions AssertJ** uniquement (`assertThat(...).isEqualTo(...)`, `.containsExactly(...)`, `.isInstanceOf(...)`, `assertThatNullPointerException()`). Pas de `assertEquals` JUnit.
- **`@ParameterizedTest`** seulement quand la répétition d'une même propriété sur plusieurs valeurs *fait partie de la leçon* (ex. associativité). Sinon, un test simple.
- **`var`** et **lambdas / références de méthode** comme dans le cours. Quand une comparaison JDK/Vavr est l'objet du cours, écrire les deux versions côte à côte.
- **Code volontairement fautif** (qui ne compile pas, ou qui lèverait une erreur) : le mettre **en commentaire** avec une phrase qui explique la règle (voir `capturingLocalVariableMustBeEffectivelyFinal`).
- **Pas de Javadoc formelle.** Des commentaires `//` courts, en français, qui suivent le style existant. Densité : un commentaire pour chaque geste non évident, pas un commentaire par ligne.
- Langue : **français** pour les commentaires et les `@DisplayName`, noms Java (méthodes, variables) en anglais ou en français selon le cours existant — rester cohérent avec le fichier voisin.

## 4. Ce qu'il ne faut PAS faire

Le but est l'enseignement, pas la robustesse en production. **Ne pas ajouter** :

- de l'**over-engineering** : pas de hiérarchie d'abstractions, de classes utilitaires, de builders, de fabriques, de `TestFixture`, de base class partagée ;
- de la **réflexion**, des **proxies**, de la **génération de code**, des **mocks** (Mockito), des **frameworks d'injection** ;
- des **dépendances** nouvelles dans `build.gradle.kts` (on n'utilise que ce qui est déjà là : JUnit, AssertJ, Vavr) ;
- des tests de **cas limites exhaustifs** (fuzzing, propriétés aléatoires, tests de performance, de concurrence) qui n'ont pas de rapport avec la leçon ;
- des **annotations** ou **constructions** non vues dans le cours ;
- de la **gestion d'erreurs** « défensive » qui noie le propos (try/catch généraux, logs, messages d'erreur personnalisés) ;
- des **refactorings** de tests existants ou de code de production pendant qu'on écrit une classe d'illustration, sauf demande explicite ;
- des **sujets hors cours** : « bonnes pratiques d'entreprise », architecture hexagonale, etc. Si une remarque de ce type paraît utile, la mettre en une phrase de commentaire au plus, ou la proposer à l'enseignant dans le message final, sans l'écrire.

Règle d'or : **si une ligne n'aide pas l'étudiant à comprendre la notion, elle n'a pas sa place.**

## 5. Méthode de travail

1. **Lire** le passage du `cours-streams-java25-fp.md` concerné, et les fichiers de test voisins, pour reprendre le vocabulaire et l'ordre pédagogique.
2. **Lister** les idées à illustrer (une par `@Nested`), puis les tests (une phrase par test) avant d'écrire.
3. **Écrire** la classe dans le package `org.example.moduleNN`, en suivant le gabarit de la section 3.
4. **Vérifier** : compilation et tests, **en local**, avec le wrapper Gradle :
   - toute la suite : `./gradlew test` ;
   - une seule classe : `./gradlew test --tests "org.example.moduleNN.NotionTest"`.
   - Ne jamais annoncer un test comme « passé » s'il n'a pas été réellement exécuté. Si la vérification n'a pas été faite, le dire explicitement.
5. **Relire** en se mettant à la place d'un étudiant : chaque `@DisplayName` se comprend-il seul ? Chaque commentaire apporte-t-il quelque chose ? Y a-t-il une ligne superflue à retirer ?

## 6. Format de la réponse finale de l'agent

Court, sans répétition du code :

- fichiers créés ou modifiés (chemins) ;
- notion(s) illustrée(s) et ordre des `@Nested` ;
- état de la vérification locale (`./gradlew test` : **exécuté et réussi**, **exécuté et en échec** avec la sortie, ou **non exécuté**) ;
- choix d'interprétation faits en l'absence de consigne, pour que l'enseignant puisse les corriger.

## 7. Résumé en une ligne

> Écrire, en français, des tests JUnit + AssertJ (+ Vavr pour la comparaison) qui **démontrent** une notion du cours, avec des commentaires qui **expliquent le pourquoi**, et rien de plus.
