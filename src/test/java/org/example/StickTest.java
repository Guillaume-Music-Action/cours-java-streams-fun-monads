package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StickTest {

    @Test
    @DisplayName("personne ne tient le baton")
    void test01() {

        //exemple pour vous montrer AssertJ, ne pas conserver
        assertThat(message)
                .isNotNull()
                .startsWith("Talking")
                .contains("Stick")
                .endsWith("Kata")
                .hasSize(18);
    }
}
