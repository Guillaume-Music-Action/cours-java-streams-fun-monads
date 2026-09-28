package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Jupiter Test Suite with Modern Assertions")
class ModernJupiterTest {

    @Test
    @DisplayName("should verify string properties fluently")
    void shouldVerifyStringPropertiesFluently() {
        var message = "Talking Stick Kata";

        assertThat(message)
                .isNotNull()
                .startsWith("Talking")
                .contains("Stick")
                .endsWith("Kata")
                .hasSize(18);
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
