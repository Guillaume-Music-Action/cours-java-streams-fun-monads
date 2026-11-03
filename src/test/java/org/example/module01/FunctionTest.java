package org.example.module01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("Module 1 - Function")
class FunctionTest {

    @Nested
    @DisplayName("application")
    class Application {

        @Test
        @DisplayName("apply transforms an input into an output")
        void applyTransformsInput() {
            Function<String, Integer> length = String::length;

            assertThat(length.apply("Java 25")).isEqualTo(7);
        }

        @Test
        @DisplayName("identity returns its input unchanged")
        void identityReturnsInput() {
            Function<String, String> identity = Function.identity();

            assertThat(identity.apply("streams")).isEqualTo("streams");

            Function<String, String> identity2 = (s) -> s;
            assertThat(identity2.apply("streams")).isEqualTo("streams");

            Function<Double, Integer> identity3 =  (d) -> d.intValue();  //Double::intValue;
            assertThat(identity3.apply(99.0)).isEqualTo(99);

            // en C# et en Kotlin on l'écrit comme ceci
            var identity4 =   (Function<Integer, Integer>) (s) -> s;
            assertThat(identity4.apply(42)).isEqualTo(42);

            var indentity5 = identity4;

            assertThat( identity3.getClass()  ).isNotEqualTo( identity4.getClass());

            assertThat(identity4).isInstanceOf(Function.class);

            //aller plus loin pour la vérification des types ne sert à rien (over engineering avec la Reflexion)

        }

        @Test
        @DisplayName("identity returns its input unchanged")
        void identityReturnsIntInput() {
            var  identity = Function.identity(); // Object to Object

            assertThat(identity.apply(true)).isEqualTo(18);
        }



        @Test
        @DisplayName("a lambda and a method reference behave the same")
        void lambdaAndMethodReferenceAreEquivalent() {
            Function<String, String> byLambda = s -> s.toUpperCase();
            Function<String, String> byReference = String::toUpperCase;

            assertThat(byLambda.apply("fp")).isEqualTo(byReference.apply("fp"));

            var autreLambda = (Function<String, String>) s  -> s + s ;
            assertThat( autreLambda.apply("z") ).isEqualTo( "zz");
        }
    }

    @Nested
    @DisplayName("composition")
    class Composition {

        private final Function<Integer, Integer> doubleIt = x -> x * 2;
        private final Function<Integer, Integer> plusOne = x -> x + 1;

        @Test
        @DisplayName("andThen applies the receiver first, then the argument")
        void andThenAppliesReceiverFirst() {
            // (x * 2) + 1
            assertThat(doubleIt.andThen(plusOne).apply(5)).isEqualTo(11);
            // f-> g -> (x)
            // cliquer sur "andThen" pour voir la signature et l'implémentation
        }

        @Test
        @DisplayName("compose applies the argument first, then the receiver")
        void composeAppliesArgumentFirst() {
            // (x + 1) * 2
            assertThat(doubleIt.compose(plusOne).apply(5)).isEqualTo(12);
            // f o g -> (x)
        }



        @Test
        @DisplayName("andThen(identity) and compose(identity) change nothing")
        void identityIsNeutralForComposition() {
            Function<Integer, Integer> id = Function.identity();

            assertThat(doubleIt.andThen(id).apply(5)).isEqualTo(doubleIt.apply(5));
            assertThat(doubleIt.compose(id).apply(5)).isEqualTo(doubleIt.apply(5));
        }

        @ParameterizedTest
        @ValueSource(ints = {-3, 0, 1, 42, Integer.MAX_VALUE / 4})
        @DisplayName("composition is associative: (f andThen g) andThen h == f andThen (g andThen h)")
        void andThenIsAssociative(int x) {
            Function<Integer, Integer> f = doubleIt;
            Function<Integer, Integer> g = plusOne;
            Function<Integer, Integer> h = n -> n * n;

            assertThat(f.andThen(g).andThen(h).apply(x))
                    .isEqualTo(f.andThen(g.andThen(h)).apply(x));
        }

        @Test
        @DisplayName("a long pipeline can be built by chaining andThen")
        void chainingBuildsPipeline() {
            Function<String, String> pipeline = String::trim;
            pipeline = pipeline.andThen(String::toLowerCase)
                               .andThen(s -> s.replace(' ', '-'));

            assertThat(pipeline.apply("  Streams JAVA 25  ")).isEqualTo("streams-java-25");
        }

        @Test
        @DisplayName("null function arguments are rejected")
        void nullFunctionsAreRejected() {
            assertThatNullPointerException().isThrownBy(() -> doubleIt.andThen(null));
            assertThatNullPointerException().isThrownBy(() -> doubleIt.compose(null));
        }
    }

    @Nested
    @DisplayName("higher-order functions")
    class HigherOrder {

        HigherOrder(){
        }

        // Takes a function and returns a new one: a function that applies f twice.
        // public + static : réutilisée telle quelle dans la classe de test Vavr
        // (FunctionVavrTest), pour comparer la version "maison" à Function0..Function8.
        public static Function<Integer, Integer> twice(Function<Integer, Integer> f) {
            return f.andThen(f);
        }

        // Curryfication : une fonction à 2 arguments (a, b) -> a + b, réécrite comme
        // une chaîne de fonctions à 1 argument : a -> (b -> a + b).
        // C'est le mécanisme qui permet l'application partielle juste en-dessous
        // (adder.apply(5) fixe le premier argument et renvoie une Function<Integer, Integer>),
        // sans avoir besoin d'une interface BiFunction dédiée ni d'une syntaxe spéciale :
        // seule l'interface Function (à 1 argument) est nécessaire, à chaque étage.
        public static final Function<Integer, Function<Integer, Integer>> adder = a -> b -> a + b;
        // public + static : réutilisée telle quelle dans la classe de test Vavr,
        // où l'on comparera avec Function2<Integer, Integer, Integer>::curried.

        @Test
        @DisplayName("a function can take a function and return a function")
        void functionTakesAndReturnsFunction() {
            Function<Integer, Integer> plusTwo = twice(x -> x + 1);

            Function<Integer, Integer> incrementeur = x -> x + 1;
            plusTwo = twice(incrementeur);

            assertThat(twice(x -> x + 3).apply(10)).isEqualTo(16);
            assertThat(plusTwo.andThen(x -> x + 1).apply(0)).isEqualTo(3);
        }

        @Test
        @DisplayName("currying: partially applying a two-argument function")
        void curryingPartialApplication() {
            // adder.apply(5) ne calcule rien : il renvoie la fonction "b -> 5 + b",
            // en gardant 5 "en mémoire" (fermeture/closure). Rien n'est additionné
            // tant que le second argument n'est pas fourni par un second apply().
            Function<Integer, Integer> addFive = adder.apply(5);

            assertThat(addFive.apply(10)).isEqualTo(15);

            // Les deux apply() s'enchaînent comme un appel à 2 arguments classique :
            // adder.apply(1).apply(2) joue le rôle de adder(1, 2) dans un langage
            // qui aurait directement une BiFunction, mais ici tout reste composable
            // avec le même vocabulaire (andThen, compose, identity) que les Function
            // à 1 argument testées plus haut dans cette classe.
            assertThat(adder.apply(1).apply(2)).isEqualTo(3);
        }

        @Test
        @DisplayName("functions are values: they can be stored in a list and applied later")
        void functionsAreValues() {
            List<Function<Integer, Integer>> steps = List.of(x -> x + 1, x -> x * 10, x -> x - 3);

            Function<Integer, Integer> all = steps.stream()
                    .reduce(Function.identity(), (f, g) -> f.andThen(g));

            assertThat(all.apply(1)).isEqualTo(17);
            assertThat(all.apply(0)).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("closures")
    class Closures {

        // adder.apply(5), vu plus haut dans HigherOrder, crée en réalité une closure :
        // la lambda "b -> a + b" capture la variable locale "a" (ici 5) de son environnement
        // au moment de sa création, et la garde disponible pour un appel ultérieur.

        @Test
        @DisplayName("a lambda captures an effectively-final local variable from its scope")
        void lambdaCapturesEffectivelyFinalLocal() {
            final int captured = 10; // jamais réassignée après son initialisation : "effectively final"

            Function<Integer, Integer> addCaptured = x -> x + captured;

            assertThat(addCaptured.apply(5)).isEqualTo(15);
            // essayer de modifier captured ici
            //captured = 0;

            assertThat(addCaptured.apply(5)).isEqualTo(15);
        }

        @Test
        @DisplayName("each call creates an independent closure, with its own captured value")
        void eachInvocationCapturesItsOwnValue() {
            Function<Integer, Integer> addFive = HigherOrder.adder.apply(5);
            Function<Integer, Integer> addHundred = HigherOrder.adder.apply(100);

            // Les deux closures ont chacune capturé un "a" différent (5 et 100) :
            // appeler l'une ne modifie pas l'autre, il n'y a pas d'état partagé entre elles.
            assertThat(addFive.apply(1)).isEqualTo(6);
            assertThat(addHundred.apply(1)).isEqualTo(101);
            assertThat(addFive.apply(2)).isEqualTo(7); // toujours le même résultat : pas de fuite
        }

        @Test
        @DisplayName("a local variable captured by a lambda must be effectively final")
        void capturingLocalVariableMustBeEffectivelyFinal() {
            // Ne compilerait pas : "local variables referenced from a lambda expression
            // must be final or effectively final" si on réassignait `total` après coup :

            /*
               int tota = 0;
               Function<Integer, Integer> addToTotal = x -> x + tota;
               tota = 5; // <-- rendrait la capture ci-dessus invalide
            //*/

            // CONTOURNEMENT :
            // Pour accumuler un état malgré tout, il faut capturer un objet MUTABLE
            // (sa référence, elle, reste effectively final) plutôt que réassigner une primitive.
            int[] total = {1};
            Function<Integer, Integer> accumulate = x -> total[0] += x;

            assertThat(accumulate.apply(3)).isEqualTo(4);  // 1 plus 3
            assertThat(total[0]).isEqualTo(4);
            assertThat(accumulate.apply(3)).isEqualTo(7);  // 4 plus 3
            assertThat(total[0]).isEqualTo(7);  // et pas 4 !!!!

            Function<Integer, Integer> noAccumulate = x -> x += x;
            assertThat(noAccumulate.apply(3)).isEqualTo(6);
            assertThat(noAccumulate.apply(3)).isEqualTo(6); //idempotente, pas d'effet de bord

            // En JavaScript, ce contournement n'est pas nécessaire : une closure JS capture
            // le binding (la variable elle-même), pas une copie de sa valeur. On peut donc
            // réassigner directement une variable locale capturée :
            //
            //   let total = 0;
            //   const accumulate = x => total += x;
            //   accumulate(3); // total == 3, pas besoin de tableau
            //
            // Java copie la VALEUR dans la lambda au moment de sa création (d'où la règle
            // "effectively final" : le compilateur doit être certain que cette valeur ne
            // changera plus). C'est lié à la pile d'appels : une variable locale vit sur la
            // pile de la méthode qui l'a déclarée, laquelle peut disparaître avant l'appel de
            // la lambda si celle-ci est renvoyée ou stockée (la lambda, elle, vit sur le tas).
            // JS n'a pas cette contrainte : dès qu'une fonction interne peut capturer une
            // variable, le moteur alloue son binding dans un environnement de closure sur le
            // tas, donc la réassigner après coup reste cohérent.
        }

        // Un value object immuable : une "opération" ne modifie rien, elle renvoie une
        // COPIE portant le nouvel état. L'état est une valeur, jamais un champ mutable.
        record BankAccount(String owner, int balance, List<String> operations) {

            void append2(String label) {
                operations.add(label);
               // balance = 0;
            }

            BankAccount deposit(int amount) {
                return new BankAccount(owner, balance + amount, append("deposit " + amount));
            }

            BankAccount withdraw(int amount) {
                return new BankAccount(owner, balance - amount, append("withdraw " + amount));
            }

            // Java n'a AUCUN opérateur de "spread" pour insérer une liste dans une autre :
            //   List.of(*operations, label)   // Kotlin : listOf(*operations.toTypedArray(), label)
            //   [..operations, label]         // C# 12 : collection expressions
            // La seule façon d'ajouter un élément est de recopier explicitement l'ancienne
            // liste, ici via Stream.concat (l'ancienne liste, elle, n'est jamais modifiée).
            private List<String> append(String label) {
                return Stream.concat(operations.stream(), Stream.of(label)).toList();
            }
        }

        @Test
        @DisplayName("an operation on a record returns a new copy instead of mutating it")
        void recordOperationReturnsACopy() {
            var account = new BankAccount("Ada", 100, List.of());

            var afterDeposit = account.deposit(50);

            assertThat(account.balance()).isEqualTo(100);      // l'original n'a pas bougé
            assertThat(afterDeposit.balance()).isEqualTo(150); // seule la copie porte le nouvel état
            assertThat(afterDeposit.owner()).isEqualTo("Ada");
            assertThat(account.operations()).isEmpty();        // l'historique de l'original est intact
            assertThat(afterDeposit.operations()).containsExactly("deposit 50");
        }

        @Test
        @DisplayName("Java has no spread operator: appending to a list means copying it")
        void noSpreadOperator() {
            var account = new BankAccount("Ada", 100, List.of("open"));

            var afterDeposit = account.deposit(50);

            // Ce qu'on écrirait en Kotlin (listOf(*operations.toTypedArray(), label)) ou en
            // C# 12 ([..operations, label]) n'a pas d'équivalent Java : il faut recopier
            // l'ancienne liste élément par élément, puis ajouter le nouveau.
            assertThat(afterDeposit.operations()).containsExactly("open", "deposit 50");
            assertThat(account.operations()).containsExactly("open"); // l'ancienne liste reste intacte
        }

        @Test
        @DisplayName("a partially applied operation is a closure over the account state")
        void operationAsClosure() {
            // deposit(50) "à moitié appliqué" : la lambda capture le montant 50 et devient
            // une Function<BankAccount, BankAccount> réutilisable, exactement comme
            // HigherOrder.adder.apply(5) capturait le 5. L'état de compte, lui, n'est
            // jamais capturé : il est passé en argument à chaque appel.
            Function<BankAccount, BankAccount> depositFifty = account -> account.deposit(50);

            var account = new BankAccount("Ada", 100, List.of());

            assertThat(depositFifty.apply(account).balance()).isEqualTo(150);
            assertThat(depositFifty.apply(account).balance()).isEqualTo(150); // pas d'état partagé, idempotence
            assertThat(depositFifty.apply(account).balance()).isEqualTo(150);

            var newState = depositFifty.apply(account);
            assertThat(depositFifty.apply(newState).balance()).isEqualTo(200);

            // Comme toute Function, ces opérations se composent : dépôt puis retrait.
            Function<BankAccount, BankAccount> withdrawTwenty = account2 -> account2.withdraw(20);
            var finalAccount = depositFifty.andThen(withdrawTwenty).apply(account);

            assertThat(finalAccount.balance()).isEqualTo(130);
            assertThat(finalAccount.operations()).containsExactly("deposit 50", "withdraw 20");
        }

        ///////////////     continuoons sur les closures de fonctions
        private int instanceField = 1;

        @Test
        @DisplayName("a lambda inside an instance method can freely read and write an instance field")
        void lambdaCapturesInstanceFieldFreely() {
            // Un champ d'instance n'est pas soumis à la règle "effectively final" :
            // la lambda capture en réalité la référence "this", pas le champ directement,
            // donc elle peut le réassigner librement, contrairement à une variable locale.
            Function<Integer, Integer> incrementField = x -> instanceField += x;

            assertThat(incrementField.apply(10)).isEqualTo(11);
            assertThat(instanceField).isEqualTo(11);
            incrementField.apply(1);
            assertThat(instanceField).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("purity and referential transparency")
    class Purity {

        @Test
        @DisplayName("a pure function returns the same result for the same input")
        void pureFunctionIsReferentiallyTransparent() {
            Function<Integer, Integer> square = x -> x * x;

            assertThat(square.apply(7)).isEqualTo(square.apply(7)).isEqualTo(49);
        }

        @Test
        @DisplayName("an impure function (hidden state) is not referentially transparent")
        void impureFunctionDependsOnHiddenState() {
            int[] counter = {0};
            Function<Integer, Integer> impure = x -> x + ++counter[0];

            assertThat(impure.apply(10)).isEqualTo(11);
            assertThat(impure.apply(10)).isEqualTo(12);
        }

        @Test
        @DisplayName("a pure function does not mutate its input")
        void pureFunctionDoesNotMutateInput() {
            Function<List<Integer>, List<Integer>> doubleAll = xs -> xs.stream()
                    .map(x -> x * 2)
                    .toList();
            var original = new ArrayList<>(List.of(1, 2, 3));

            var result = doubleAll.apply(original);

            assertThat(original).containsExactly(1, 2, 3);
            assertThat(result).containsExactly(2, 4, 6);
        }
    }

    @Nested
    @DisplayName("with streams")
    class WithStreams {

        @Test
        @DisplayName("map(f.andThen(g)) gives the same result as map(f).map(g)")
        void mapFusesComposedFunctions() {
            Function<Integer, Integer> f = x -> x + 1;
            Function<Integer, Integer> g = x -> x * 2;

            var composed = Stream.of(1, 2, 3).map(f.andThen(g)).toList();
            var chained = Stream.of(1, 2, 3).map(f).map(g).toList();

            assertThat(composed).isEqualTo(chained).containsExactly(4, 6, 8);
        }

        @Test
        @DisplayName("Function.identity() is a convenient mapper that keeps elements as they are")
        void identityAsMapper() {
            var words = Stream.of("a", "b", "c").map(Function.identity()).toList();

            assertThat(words).containsExactly("a", "b", "c");
        }
    }
}
