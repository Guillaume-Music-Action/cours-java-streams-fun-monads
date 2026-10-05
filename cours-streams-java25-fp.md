# Cours : Streams Java 25 et programmation fonctionnelle

> Plan de cours, sans exemples de code : ceux-ci viendront pendant les séances, en TDD.
> Fil conducteur : partir de ce que le JDK offre, repérer ses limites, puis montrer où Vavr comble le manque.

---

## Partie A : Plan du cours (JDK 25)

### Module 0 : Cadrage
- Pourquoi la programmation fonctionnelle : fonctions pures, immutabilité, composition, absence d'effets de bord
- Ce que Java offre (lambdas, interfaces fonctionnelles, records, sealed types, pattern matching) et ce qu'il n'offre pas
- Le Stream comme pipeline déclaratif : source → opérations intermédiaires → opération terminale
- Paresse, usage unique, court-circuit

### Module 1 : Fondations
- Interfaces fonctionnelles : `Function`, `Predicate`, `Supplier`, `Consumer`, `BiFunction`, `UnaryOperator`
- Références de méthodes et composition (`andThen`, `compose`, `negate`)
- Fonctions d'ordre supérieur
- Pureté et transparence référentielle : comment les reconnaître, comment les tester
- Création de streams : collections, tableaux, `Stream.of`, `iterate`, `generate`, streams primitifs (`IntStream`, etc.)

### Module 2 : Projections (map)
- `map`, `mapToInt` / `mapToObj`, `boxed`
- Le map comme foncteur : conservation de la structure, lois d'identité et de composition
- `peek` et pourquoi on l'évite

### Module 3 : Filtres et sélection
- `filter`, `takeWhile`, `dropWhile`, `distinct`, `limit`, `skip`
- Prédicats composés
- Ordre des opérations et impact sur la paresse et les performances

### Module 4 : Aplatissement
- `flatMap`, `mapMulti`, `flatMapToInt`
- Relation avec `Optional` et les relations 1-N
- Choix entre `flatMap` et `mapMulti`

### Module 5 : Réduction et fold
- `reduce` (formes à 1, 2 et 3 arguments)
- Élément neutre, associativité, rôle du monoïde
- Réduction immuable (`reduce`) versus réduction mutable (`collect`)
- Fold gauche et droite : ce qu'un stream peut ou ne peut pas exprimer
- `Gatherers.fold` et `Gatherers.scan` : fold et accumulation avec états intermédiaires
- Réductions utilitaires : `count`, `sum`, `min` / `max`, `anyMatch` / `allMatch` / `noneMatch`, `findFirst` / `findAny`

### Module 6 : Collecte, regroupement et partitionnement
- `Collectors` : `toList`, `toMap`, `joining`, `summarizing*`
- `groupingBy` (classifieur, map factory, downstream collectors)
- `partitioningBy`
- `teeing` : deux réductions en un seul passage
- Écrire son propre `Collector` : supplier, accumulator, combiner, finisher
- Collecteurs en cascade (`mapping`, `filtering`, `flatMapping`, `reducing`)

### Module 7 : Gatherers (JEP 485)
- Le problème : les opérations intermédiaires manquantes
- Anatomie d'un `Gatherer` : initializer, integrator, combiner, finisher
- Gatherers fournis : `windowFixed`, `windowSliding`, `scan`, `fold`, `mapConcurrent`
- Composition de gatherers (`andThen`)
- Écrire son propre gatherer

### Module 8 : Zip et combinaison de flux
- Pourquoi il n'y a pas de `zip` natif et ce que ça dit du modèle
- Zip par index, zip de deux streams, zip avec fonction de combinaison
- Implémentation via gatherer
- `concat`, produit cartésien via `flatMap`
- `zipWithIndex`, `unzip` (par `teeing` ou collecte)

### Module 9 : Apply et application de fonctions
- Appliquer une fonction à une valeur dans un contexte : de `map` à `apply`
- Foncteur contre applicative
- Currying et application partielle en Java
- Combiner plusieurs valeurs contextuelles indépendantes (`Optional`, résultats de validation)

### Module 10 : Stream et effets
- **Quand a-t-on affaire à des effets ?**
  - Dès qu'un calcul fait autre chose que renvoyer une valeur pure dépendant uniquement de ses paramètres d'entrée :
    - *Effets d'échec / d'absence* : division par zéro, ressource introuvable (`Optional`, `Result`, `Try`, `Either`).
    - *Effets de bord physiques (I/O)* : écrire en console, interroger une base de données, appeler une API REST, lire l'horloge système (`IO`).
    - *Effets contextuels / environnement* : accéder à une configuration, une transaction ou un contexte d'authentification sans le passer partout manuellement (`Reader`).
    - *Effets de journalisation / audit* : accumuler des logs ou des métriques au fil de l'eau sans logger global mutable (`Writer`).
    - *Effets de transition d'état* : faire évoluer un état (ex. compteur, pseudo-aléatoire, accumulateur complexe) de façon déterministe sans variable mutable (`State`).
- **Pourquoi a-t-on besoin de les modéliser comme des valeurs ?**
  - En programmation fonctionnelle, les effets non encapsulés brisent la transparence référentielle, empêchent la composition des pipelines et rendent les tests unitaires fragiles (recours obligé aux mocks).
  - Encapsuler un effet dans un type permet de raisonner dessus, de tester le comportement à froid sans exécuter l'effet immédiatement, et de composer les flux d'effets avec `flatMap`.
  - Pour les monades d'effets avancées (`IO`, `Reader`, `Writer`, `State`), se reporter à l'[Annexe : Au-delà de Vavr](#annexe--au-delà-de-vavr--monades-deffets-state-reader-writer-io).
- Effets de bord : `forEach` et `forEachOrdered`
- Gestion des erreurs dans un pipeline : exceptions vérifiées, pourquoi elles cassent la composition
- Représenter l'échec comme une valeur (`Optional`, type `Result` avec sealed types et records)
- Parallélisme : `parallel()`, conditions de sûreté, pièges (état partagé, ordre, associativité)
- Streams et concurrence structurée / threads virtuels (`mapConcurrent`)

### Module 11 : Ouverture vers les monades
- Rappel du motif : un type enveloppe + `map` + `flatMap` (bind) + injection (`of` / `unit`)
- Les trois lois : identité gauche, identité droite, associativité
- **Démystifier la formule : « une monade n'est qu'un monoïde dans la catégorie des endofoncteurs »** :
  - *Endofoncteur* : en Java, c'est simplement un type générique `F<T>` muni d'un `map` qui transforme les valeurs sans sortir du système de types Java (de la catégorie des types Java vers elle-même).
  - *Monoïde* : c'est la même structure vue au Module 5 (un ensemble, une opération binaire associative et un élément neutre), mais transposée aux types emboîtés plutôt qu'aux valeurs :
    - L'élément neutre est l'injection : `unit` / `of` (transforme un `T` en `F<T>`).
    - L'opération associative est l'aplatissement : `flatten` / `join` (transforme un `F<F<T>>` en `F<T>`).
  - *Mise en pratique TDD* : montrer concrètement que `flatMap(f)` n'est que la composition `map(f)` suivie de `flatten()`, et vérifier par tests de propriété que `flatten` respecte l'associativité et l'élément neutre (`unit`).
- `Optional`, `Stream`, `CompletableFuture` : monades, quasi-monades ou pas ?
- Nuances : usage unique et paresse du `Stream`, absence de typeclass en Java
- Construire un type `Result` / `Either` monadique
- Pipelines monadiques : chaîner des opérations faillibles sans exceptions
- Aperçu : `State`, `Reader`, `Writer`, `IO` et ce qu'ils apportent (voir Annexe)

### Fil rouge pour la mise en TDD
- Chaque module se prête à un test de propriété (lois du foncteur, du monoïde, de la monade)
- Progression suggérée : implémenter soi-même `map`, `filter`, `fold`, `zip` et `Result` sur une structure maison, puis comparer avec l'API JDK
- Réserver la section « parallélisme » et les gatherers personnalisés aux étudiants les plus avancés

---

## Partie B : Quand le JDK ne suffit plus, et où Vavr prend le relais

### B.1 Principe directeur

Le JDK 25 permet d'écrire des **pipelines** fonctionnels sur des données (Streams + Gatherers) et de **modéliser** des données de façon algébrique (records, sealed interfaces, pattern matching). Il ne fournit pas la **boîte à outils de valeurs fonctionnelles** autour : types d'erreur, validation cumulative, collections persistantes, fonctions curryfiées, tuples. C'est là que Vavr entre en jeu.

Règle pratique : **on reste sur le JDK tant que le problème est un traitement de données en flux ; on ajoute Vavr dès qu'on veut traiter l'échec, l'absence, le calcul différé ou la combinaison comme des valeurs de première classe.**

### B.2 Les limites du JDK, module par module

| Module | Limite du JDK | Apport de Vavr |
|---|---|---|
| 0 / 1 | Interfaces fonctionnelles limitées (`Function`, `BiFunction`, pas de `Function3`+), pas de lambdas qui lèvent des exceptions vérifiées | `Function0..8`, `CheckedFunction0..8`, `lift`, `memoized`, `curried`, `tupled`, `reversed` |
| 1 | Pas de tuples standard (il faut définir un record à chaque fois) | `Tuple1..8` avec `map`, `apply`, `append` |
| 1 / 5 | Collections immuables (`List.of`) mais **non persistantes** : toute modification copie. `Collections.unmodifiableList` n'est qu'une vue | Collections persistantes à partage structurel : `List`, `Vector`, `Queue`, `HashMap`, `TreeMap`, `HashSet`, `TreeSet`, `LinkedHashMap`, etc. |
| 2 / 3 / 5 | Il faut toujours passer par `.stream()` puis `.collect()` ou `.toList()` pour transformer une collection | Les collections Vavr portent directement `map`, `filter`, `flatMap`, `foldLeft`, `foldRight`, `reduce`, `scanLeft`, `scanRight`, `take`, `drop`, `takeWhile`, `dropWhile`, `distinct` |
| 5 | Pas de `foldRight` sur Stream ; `reduce` impose le même type ou un combiner. `Gatherers.fold` et `scan` existent mais restent dans la logique d'un seul sens | `foldLeft` et `foldRight` natifs, avec type de résultat libre |
| 6 | `groupingBy` et `partitioningBy` passent par des collecteurs verbeux et retournent des `Map` mutables | `groupBy`, `partition`, `grouped`, `sliding` directement sur la collection, résultat persistant |
| 8 | Pas de `zip` natif (à écrire en gatherer ou via index) | `zip`, `zipWith`, `zipWithIndex`, `unzip`, `crossProduct`, `intersperse` |
| 8 | Stream à usage unique : impossible de réutiliser ni de rejouer | `io.vavr.collection.Stream` : paresseux **et** réutilisable, mémoïsé |
| 9 | **Aucun applicatif** : `Optional` et `CompletableFuture` offrent `flatMap` mais pas de combinaison indépendante de N valeurs | `Validation.combine(...)` accumule **toutes** les erreurs ; `Option.sequence`, `Either.sequence`, `Try.sequence`, `traverse` |
| 10 | Pas de type d'échec dans la bibliothèque standard (`Optional` perd la cause) | `Try` (capture d'exceptions), `Either<L, R>`, `Validation<E, T>`, avec `recover`, `mapFailure`, `orElse`, `toEither` |
| 10 | Exceptions vérifiées incompatibles avec les lambdas | `Try.of(CheckedFunction0)`, `Function.lift`, `Try.withResources` |
| 11 | Pas de `Either` ni de monade d'échec officielle ; `Optional` limité (pas de `Serializable`, usage déconseillé en champ) | `Option`, `Either`, `Try`, `Lazy`, `Future` avec `map` / `flatMap` cohérents |
| 11 | Pas de for-comprehension : chaînes de `flatMap` imbriquées illisibles | `API.For(...).yield(...)` sur `Option`, `Try`, `Either`, `List`, `Future` |
| transverse | Pas de mémoïsation ni d'évaluation différée standardisée | `Lazy<T>`, `Function.memoized()` |

### B.3 Ce que Java 25 couvre déjà et rend Vavr moins nécessaire

- **Pattern matching** : `switch` avec patterns de types, patterns de records et sealed interfaces couvre la plupart des usages de `Match` / `Case` de Vavr
- **Records** : remplacent les cas simples où l'on utilisait les `Tuple` de Vavr pour du code non générique
- **Sealed types** : permettent de définir son propre `Result` ou `Either` exhaustif, sans dépendance
- **Gatherers (JEP 485)** : couvrent `windowFixed` / `windowSliding` (équivalents de `grouped` / `sliding`), `scan` (équivalent de `scanLeft`), `fold`, et permettent d'écrire un `zip`
- **Threads virtuels et concurrence structurée** : réduisent l'intérêt de `Future` de Vavr
- **`Stream.toList()`** : liste non modifiable sans collecteur

Conséquence pédagogique : une bonne partie de Vavr peut être **reconstruite en TDD** à partir du JDK 25. C'est précisément l'intérêt du cours.

### B.4 Les limites que Vavr ne lève pas non plus

- **Pas de types d'ordre supérieur (higher-kinded types)** : impossible d'écrire du code générique sur « n'importe quelle monade ». Vavr répète donc les mêmes méthodes sur chaque type sans interface commune exploitable
- **Pas de `State`, `Reader`, `Writer`, `IO`** : Vavr ne les fournit pas. Pour aller plus loin, il faut passer à une autre bibliothèque ou les écrire soi-même
- **Coût d'adoption** : types Vavr dans les signatures publiques, interopérabilité avec Spring, Jackson, JPA et les API JDK à gérer (conversions `toJavaList`, `toJavaOptional`, etc.)
- **Maintenance** : vérifier l'état du projet et la version stable courante avant de l'imposer (ligne 0.10.x historiquement stable, 1.0 longtemps en préparation). À contrôler au moment de monter le cours
- **Alternatives** à mentionner : jOOλ (streams enrichis, tuples, `zip`), Functional Java, Fugue (Atlassian)

### B.5 Arbre de décision

1. Je transforme des données en flux ? → **Streams + Gatherers (JDK)**
2. J'ai besoin d'un résultat qui peut échouer, avec la cause ? → `Result` maison (sealed + records) **ou** `Either` / `Try` (Vavr)
3. Je veux cumuler toutes les erreurs de validation ? → **Vavr `Validation`** (le JDK n'a pas d'équivalent ; sinon, l'écrire soi-même)
4. J'ai besoin de collections immuables modifiées souvent, avec historique ou partage ? → **Collections persistantes Vavr**
5. J'ai besoin de `foldRight`, `zip`, `groupBy` directement sur la collection ? → **Collections Vavr**
6. Je compose des fonctions à plus de 2 paramètres, avec currying ou mémoïsation ? → **Vavr `FunctionN`**
7. J'enchaîne des opérations qui lèvent des exceptions vérifiées ? → **Vavr `Try` / `CheckedFunction`**
8. Je veux de la lisibilité sur de longues chaînes de `flatMap` ? → **Vavr `For`**
9. Je veux du pattern matching sur types ? → **Java 25 d'abord** ; Vavr `Match` seulement pour des prédicats arbitraires
10. J'ai besoin de manipuler des effets purs (`State`, `Reader`, `Writer`, `IO`) ? → **Ni JDK ni Vavr** : à implémenter soi-même en TDD ou se tourner vers une bibliothèque spécialisée (voir [Annexe : Au-delà de Vavr](#annexe--au-delà-de-vavr--monades-deffets-state-reader-writer-io))

### B.6 Intégration dans le déroulé du cours

Proposition de séquencement : **JDK d'abord, Vavr ensuite**, toujours après avoir constaté la limite en TDD.

| Séance | Contenu JDK | Point de bascule vers Vavr |
|---|---|---|
| Modules 1 à 4 | Fonctions, map, filter, flatMap | `FunctionN`, `Tuple`, `curried`, `memoized` |
| Module 5 | reduce, collect, fold | `foldLeft` / `foldRight`, comparaison avec `Gatherers.fold` |
| Module 6 | Collectors | `groupBy` / `partition` directement sur collection Vavr |
| Modules 7 et 8 | Gatherers, zip maison | `zip` / `sliding` / `grouped` Vavr, comparaison avec gatherers |
| Module 9 | Currying, applicatif à la main | `Validation.combine`, `sequence` / `traverse` |
| Module 10 | `Result` maison, exceptions | `Try`, `Either`, `CheckedFunction`, `lift` |
| Module 11 | Lois de la monade sur `Result` maison | `Option`, `Either`, `Try`, `Lazy`, `For`, puis limites (pas de HKT, pas de `State` / `IO`) |

### B.7 Exercices-charnières à prévoir (TDD)

- Écrire un `Result<E, T>` maison, vérifier les lois de monade par tests de propriété, puis le comparer à `Either` de Vavr
- Écrire un `Validation` applicatif cumulant les erreurs et constater qu'un `Result` monadique s'arrête à la première
- Réimplémenter `zip` et `sliding` en gatherers, puis comparer à `zip` et `sliding` de Vavr
- Mesurer la différence entre modification d'une `List` JDK (copie) et d'une `List` Vavr (partage structurel)
- Rendre un pipeline avec exceptions vérifiées composable, d'abord avec un wrapper maison, puis avec `Try`

---

## Annexe : Au-delà de Vavr — Monades d'effets (`State`, `Reader`, `Writer`, `IO`)

### 1. Pourquoi Vavr ne les propose pas
Vavr a été conçue comme une boîte à outils pragmatique pour combler les manques immédiats de Java 8+ (collections persistantes, contrôle d'erreur `Try`/`Either`, tuples, arités fonctionnelles). Elle ne cherche pas à être un framework de programmation purement fonctionnelle complet et omet délibérément les monades de gestion d'état et d'effets secondaires.

### 2. Quelles bibliothèques en Java pour aller plus loin ?

- **Cyclops (`io.github.cyclops-react`)** :
  - La bibliothèque moderne la plus complète en Java pour les monades d'effets.
  - Fournit nativement : `Reader<T, R>`, `Writer<W, T>`, `State<S, T>`, `IO<T>` (paresseux et asynchrone), et structures `Free` monad / `Trampoline`.
  - Modules d'interopérabilité directe avec Vavr, Guava, RxJava et les streams du JDK.
- **Functional Java (`functionaljava.org`)** :
  - Historique et mathématiquement très rigoureuse (calquée sur Haskell / Scalaz).
  - Implémente `State<S, A>`, `Reader<R, A>`, `Writer<W, A>`, `IO<A>` avec gestion de la récursion sur la pile via trampolines.
  - Syntaxe plus verbeuse due à son antériorité aux lambdas modernes de Java.
- **Atlassian Fugue (`io.atlassian.fugue`)** :
  - FP pragmatique orientée `Option`, `Either`, `Pair`, mais n'implémente pas `State` ni `IO`.

### 3. Exercices pédagogiques en TDD (implémentations maison)

Plutôt que d'introduire un framework lourd en fin de cursus, le parti-pris pédagogique est de **coder soi-même ces abstractions d'effets en TDD** :

- **`IO<A>` (Gestion des effets de bord réels : I/O, console, réseau)** :
  - *Principe* : un `IO<A>` est une **description de calcul** (un plan d'exécution différé) et non son exécution immédiate. En Java, il s'agit fondamentalement d'un wrapper de `Supplier<A>` (ou `Callable<A>`).
  - *Intérêt pédagogique* : séparer la **définition pure** d'un programme de son **évaluation impure** (« *running at the end of the world* »). Un appel à `IO.println(...)` ou `IO.readFile(...)` ne produit aucun effet tant qu'on n'appelle pas explicitement `.unsafeRun()`.
  - *Opérations à tester* : `delay(Supplier<A>)` / `unit(A)`, `map(A -> B)`, `flatMap(A -> IO<B>)`, `unsafeRun()`, gestion de la paresse (vérifier qu'aucun effet de bord n'a lieu avant `unsafeRun()`).
- **`Reader<R, A>` (Effet de lecture d'environnement / contexte)** :
  - *Principe* : n'est fondamentalement qu'une abstraction autour d'une fonction `Function<R, A>`.
  - *Intérêt pédagogique* : modéliser une injection de dépendance et un contexte d'exécution pur, sans framework ni conteneur IoC (Spring).
  - *Opérations à tester* : `ask()`, `unit(A)`, `map(A -> B)`, `flatMap(A -> Reader<R, B>)`, `run(R)`.
- **`Writer<W, A>` (Effet de trace / journalisation pure)** :
  - *Principe* : associe une valeur produite `A` à un log/contexte accumulé `W` (où `W` forme un monoïde, e.g. `List<String>`). C'est un wrapper de `Tuple2<W, A>`.
  - *Intérêt pédagogique* : tracer l'exécution d'un calcul sans logger mutable global ni appel I/O immédiat ; les traces se composent algébriquement avec l'opération du monoïde.
  - *Opérations à tester* : `tell(W)`, `unit(A)`, `map`, `flatMap` (qui concatène les traces de deux calculs successifs).
- **`State<S, A>` (Effet d'état modifiable pur)** :
  - *Principe* : n'est qu'un wrapper d'une fonction de transition d'état `Function<S, Tuple2<S, A>>`.
  - *Intérêt pédagogique* : manipuler et propager un état immuable sans variable mutable ni effet de bord partagé.
  - *Opérations à tester* : `get()`, `set(S)`, `modify(S -> S)`, `map`, `flatMap`, `run(S)`.
