package io.synthesized.tdktc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class ContainerCompatibilityTest {
    public static final String COMPATIBLE_IMAGE = "synthesizedio/synthesized-tdk-cli:latest";
    public static final String INCOMPATIBLE_IMAGE = "rancher/cowsay:latest";
    private SynthesizedTDK synthesizedTDK;

    @BeforeEach
    void setUp() {
        synthesizedTDK = new SynthesizedTDK();
    }

    @Test
    void checkCompatibleImage() {
        assertThat(synthesizedTDK
                .validateImage(COMPATIBLE_IMAGE))
                .isTrue();
    }

    @Test
    void setCompatibleImage() {
        assertThat(synthesizedTDK
                .setImageName(COMPATIBLE_IMAGE))
                .isSameAs(synthesizedTDK);
    }

    @Test
    void checkNonCompatibleImage() {
        assertThat(synthesizedTDK
                .validateImage(INCOMPATIBLE_IMAGE))
                .isFalse();
    }

    @Test
    void setNonCompatibleImage() {
        assertThrows(IllegalArgumentException.class, () -> synthesizedTDK
                .setImageName(INCOMPATIBLE_IMAGE));
    }
}
