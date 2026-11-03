package org.example.module01;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function8;
import io.vavr.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.module01.FunctionTest.HigherOrder.adder;
import static org.example.module01.FunctionTest.HigherOrder.twice;

@DisplayName("Module 1 - Function, version Vavr")
class FunctionVavrTest {

    // Même addition que FunctionTest.HigherOrder.adder, mais écrite "à plat",
    // avec ses deux arguments, comme une BiFunction du JDK.
    // Function2 étend d'ailleurs java.util.function.BiFunction.
    static final Function2<Integer, Integer, Integer> add = (a, b) -> a + b;

    @Nested
    @DisplayName("currying")
    class Currying {

        @ParameterizedTest
        @CsvSource({"1, 2", "5, 10", "-3, 3", "0, 0"})
        @DisplayName("curried() gives the same function as the hand-made adder")
        void curriedMatchesHandMadeAdder(int a, int b) {
            // Version maison : on a écrit nous-mêmes la chaîne a -> b -> a + b.
            // Version Vavr : curried() fabrique cette chaîne à partir de la fonction à plat.
            // Le type obtenu est Function1<Integer, Function1<Integer, Integer>>,
            // le pendant exact de Function<Integer, Function<Integer, Integer>>.
            Function1<Integer, Function1<Integer, Integer>> curriedAdd = add.curried();

            assertThat(curriedAdd.apply(a).apply(b))
                    .isEqualTo(adder.apply(a).apply(b))
                    .isEqualTo(a + b);
        }

        @Test
        @DisplayName("partial application works directly, without calling curried()")
        void partialApplicationWithoutCurried() {
            // Function2.apply(T1) existe en plus de apply(T1, T2) : fournir un seul
            // argument renvoie la fonction qui attend le second (une closure sur 5).
            // Avec une BiFunction du JDK, c'est impossible sans écrire un adaptateur.
            Function1<Integer, Integer> addFive = add.apply(5);

            assertThat(addFive.apply(10)).isEqualTo(15);
        }

        @Test
        @DisplayName("curried() also works beyond two arguments")
        void curryingThreeArguments() {
            // Le JDK s'arrête à BiFunction ; Vavr va jusqu'à Function8.
            Function3<Integer, Integer, Integer, Integer> sum3 = (a, b, c) -> a + b + c;

            assertThat(sum3.curried().apply(1).apply(2).apply(3)).isEqualTo(6);
            // Application partielle sur les deux premiers arguments, le troisième plus tard.
            assertThat(sum3.apply(1, 2).apply(3)).isEqualTo(6);
        }

        @Test
        @DisplayName("Function8 is the largest arity offered by Vavr")
        void arityGoesUpToEight() {
            Function8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> sum8 =
                    (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;

            assertThat(sum8.arity()).isEqualTo(8);
            assertThat(sum8.curried()
                    .apply(1).apply(2).apply(3).apply(4)
                    .apply(5).apply(6).apply(7).apply(8))
                    .isEqualTo(36);
        }
    }

    @Nested
    @DisplayName("interoperability with java.util.function")
    class Interoperability {

        @Test
        @DisplayName("a Vavr Function1 is a JDK Function: it can be passed to the hand-made twice()")
        void vavrFunctionIsAJdkFunction() {
            // Function1<T, R> étend java.util.function.Function<T, R> :
            // twice(), écrite pour le JDK dans FunctionTest, l'accepte telle quelle.
            Function1<Integer, Integer> plusOne = x -> x + 1;

            assertThat(twice(plusOne).apply(0)).isEqualTo(2);
        }

        @Test
        @DisplayName("a JDK Function becomes a Vavr Function1 through a method reference")
        void jdkFunctionToVavrFunction() {
            // Dans l'autre sens, il faut convertir : Function1.of attend un Function1,
            // et la référence de méthode ::apply fait l'adaptation.
            Function<Integer, Integer> jdkTwice = twice(x -> x * 3);
            Function1<Integer, Integer> vavrTwice = Function1.of(jdkTwice::apply);

            assertThat(vavrTwice.apply(1)).isEqualTo(jdkTwice.apply(1)).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("what Vavr adds on top of the JDK")
    class Extras {

        @Test
        @DisplayName("tupled() turns a two-argument function into a function of a single tuple")
        void tupledTakesOneTuple() {
            // L'inverse de la curryfication : au lieu d'une chaîne de fonctions à 1 argument,
            // on regroupe les deux arguments en une seule valeur (un Tuple2).
            assertThat(add.tupled().apply(Tuple.of(2, 3))).isEqualTo(5);
        }

        @Test
        @DisplayName("reversed() swaps the order of the arguments")
        void reversedSwapsArguments() {
            Function2<String, String, String> concat = (a, b) -> a + b;

            assertThat(concat.apply("a", "b")).isEqualTo("ab");
            assertThat(concat.reversed().apply("a", "b")).isEqualTo("ba");
        }

        @Test
        @DisplayName("memoized() computes each result only once")
        void memoizedComputesOnce() {
            // Mémoïsation : memoized() renvoie une fonction qui garde en mémoire (dans un cache,
            // une Map interne) le résultat de chaque appel, indexé par l'argument.
            //   - premier appel avec x : la fonction d'origine est exécutée, le résultat est rangé ;
            //   - appel suivant avec le MÊME x : le résultat rangé est renvoyé, sans rien recalculer ;
            //   - appel avec un NOUVEAU x : le cache ne connaît pas encore la valeur, on calcule.
            // C'est intéressant quand le calcul est coûteux et que les mêmes arguments reviennent.
            //
            // Condition : la fonction doit être PURE (voir FunctionTest.Purity), c'est-à-dire
            // toujours la même sortie pour la même entrée. Sinon, le cache renverrait une
            // valeur périmée : la mémoïsation ne serait plus une simple optimisation, elle
            // changerait le résultat du programme.
            //
            // Le compteur `calls` ne sert qu'à observer les vrais appels de la fonction d'origine.
            int[] calls = {0};
            Function1<Integer, Integer> square = x -> {
                calls[0]++;
                return x * x;
            };
            Function1<Integer, Integer> memoSquare = square.memoized();

            assertThat(memoSquare.apply(4)).isEqualTo(16);   // calcul réel : calls = 1
            assertThat(memoSquare.apply(4)).isEqualTo(16);   // lu dans le cache : calls reste 1
            assertThat(calls[0]).isEqualTo(1);

            assertThat(memoSquare.apply(5)).isEqualTo(25);   // autre argument : nouveau calcul
            assertThat(calls[0]).isEqualTo(2);
        }

        // Mémoïsation et closures : memoized() est une closure. La fonction renvoyée capture
        // le cache (une Map interne), comme adder.apply(5) capturait "a" dans FunctionTest.
        // La différence : une closure classique ne fait que lire ses valeurs capturées, alors
        // que le cache est un état mutable qui grandit à chaque appel. La règle "effectively
        // final" (voir FunctionTest.Closures) ne bloque pas cette mutation, car la référence
        // au cache ne change jamais : c'est le même contournement que int[] total.
    }
}
