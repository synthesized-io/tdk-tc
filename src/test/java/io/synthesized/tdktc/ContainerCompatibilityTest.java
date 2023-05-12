package io.synthesized.tdktc;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ContainerCompatibilityTest {
    public static final DockerImageName COMPATIBLE_IMAGE = DockerImageName.parse("synthesizedio/synthesized-tdk-cli:latest");
    public static final DockerImageName INCOMPATIBLE_IMAGE = DockerImageName.parse("rancher/cowsay:latest");

    @Test
    void checkCompatibleImage() {
        assertThat(SynthesizedTDK
                .validateImage(COMPATIBLE_IMAGE))
                .isTrue();
    }


    @Test
    void checkNonCompatibleImage() {
        assertThat(SynthesizedTDK
                .validateImage(INCOMPATIBLE_IMAGE))
                .isFalse();
    }

    @Test
    void setNonCompatibleImage() {
        assertThatThrownBy(() -> new SynthesizedTDK(INCOMPATIBLE_IMAGE)
                .transform(null, null, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(INCOMPATIBLE_IMAGE.asCanonicalNameString());

    }
}
