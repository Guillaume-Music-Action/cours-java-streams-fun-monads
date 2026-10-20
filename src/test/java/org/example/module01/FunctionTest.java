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
        }

        @Test
        @DisplayName("a lambda and a method reference behave the same")
        void lambdaAndMethodReferenceAreEquivalent() {
            Function<String, String> byLambda = s -> s.toUpperCase();
            Function<String, String> byReference = String::toUpperCase;

            assertThat(byLambda.apply("fp")).isEqualTo(byReference.apply("fp"));
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
        }

        @Test
        @DisplayName("compose applies the argument first, then the receiver")
        void composeAppliesArgumentFirst() {
            // (x + 1) * 2
            assertThat(doubleIt.compose(plusOne).apply(5)).isEqualTo(12);
        }

        @Test
        @DisplayName("composition order matters: andThen and compose differ")
        void compositionOrderMatters() {
            assertThat(doubleIt.andThen(plusOne).apply(5))
                    .isNotEqualTo(doubleIt.compose(plusOne).apply(5));
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

            assertThat(pipeline.apply("  Streams Java 25  ")).isEqualTo("streams-java-25");
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

        // Takes a function and returns a new one: a function that applies f twice.
        static Function<Integer, Integer> twice(Function<Integer, Integer> f) {
            return f.andThen(f);
        }

        // Currying: takes one argument at a time.
        static final Function<Integer, Function<Integer, Integer>> adder = a -> b -> a + b;

        @Test
        @DisplayName("a function can take a function and return a function")
        void functionTakesAndReturnsFunction() {
            Function<Integer, Integer> plusTwo = twice(x -> x + 1);

            assertThat(twice(x -> x + 3).apply(10)).isEqualTo(16);
            assertThat(plusTwo.andThen(x -> x + 1).apply(0)).isEqualTo(3);
        }

        @Test
        @DisplayName("currying: partially applying a two-argument function")
        void curryingPartialApplication() {
            Function<Integer, Integer> addFive = adder.apply(5);

            assertThat(addFive.apply(10)).isEqualTo(15);
            assertThat(adder.apply(1).apply(2)).isEqualTo(3);
        }

        @Test
        @DisplayName("functions are values: they can be stored in a list and applied later")
        void functionsAreValues() {
            List<Function<Integer, Integer>> steps = List.of(x -> x + 1, x -> x * 10, x -> x - 3);

            Function<Integer, Integer> all = steps.stream()
                    .reduce(Function.identity(), (f, g) -> f.andThen(g));

            assertThat(all.apply(1)).isEqualTo(17);
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
