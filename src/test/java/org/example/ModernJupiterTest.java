package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import io.vavr.control.Option;
import io.vavr.control.Try;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Jupiter Test Suite with Modern Assertions")
class ModernJupiterTest {

    @Test
    @DisplayName("should verify string properties fluently")
    void shouldVerifyStringPropertiesFluently() {
        var message = "Java 25 Streams & Functional Programming";

        assertThat(message)
                .isNotNull()
                .startsWith("Java 25")
                .contains("Streams")
                .endsWith("Programming");
    }

    @Test
    @DisplayName("should verify collections fluently")
    void shouldVerifyCollectionsFluently() {
        var speakers = List.of("Alice", "Bob", "Charlie");

        assertThat(speakers)
                .isNotEmpty()
                .hasSize(3)
                .containsExactly("Alice", "Bob", "Charlie")
                .doesNotContain("Dave");
    }

    @Test
    @DisplayName("should verify optional values fluently")
    void shouldVerifyOptionalValuesFluently() {
        var currentSpeaker = Optional.of("Alice");

        assertThat(currentSpeaker)
                .isPresent()
                .hasValue("Alice");
    }

    @Test
    @DisplayName("should verify Vavr functional controls fluently")
    void shouldVerifyVavrFunctionalControlsFluently() {
        var option = Option.of("Stream");
        var successfulTry = Try.of(() -> Integer.parseInt("42"));
        var failedTry = Try.of(() -> Integer.parseInt("abc"));

        assertThat(option.isDefined()).isTrue();
        assertThat(option.get()).isEqualTo("Stream");
        assertThat(successfulTry.isSuccess()).isTrue();
        assertThat(successfulTry.get()).isEqualTo(42);
        assertThat(failedTry.isFailure()).isTrue();
        assertThat(failedTry.getCause())
                .isInstanceOf(NumberFormatException.class);
    }

    @Nested
    @DisplayName("when testing composite objects")
    class CompositeObjectTests {

        record Participant(String name, int speakingTurns) {}

        @Test
        @DisplayName("should verify object fields and conditions")
        void shouldVerifyObjectFieldsAndConditions() {
            var participant = new Participant("Alice", 2);

            assertThat(participant)
                    .extracting(Participant::name, Participant::speakingTurns)
                    .containsExactly("Alice", 2);

            assertThat(participant.speakingTurns())
                    .isPositive()
                    .isLessThan(5);
        }
    }
}
