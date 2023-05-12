package io.synthesized.tdktc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

public final class SynthesizedTDK {

    public static final DockerImageName DEFAULT_IMAGE_NAME =
            DockerImageName.parse("synthesizedio/synthesized-tdk-cli");
    private static final Logger LOGGER = LoggerFactory.getLogger(SynthesizedTDK.class);

    private final DockerImageName dockerImageName;
    private String license;

    public SynthesizedTDK(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public SynthesizedTDK(final DockerImageName dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    /**
     * Sets license key for paid version of TDK.
     *
     * @param license License key.
     */
    public SynthesizedTDK setLicense(String license) {
        this.license = license;
        return this;
    }

    public void transform(JdbcDatabaseContainer<?> input, JdbcDatabaseContainer<?> output, String config) {
        if (!dockerImageName.isCompatibleWith(DEFAULT_IMAGE_NAME) && !validateImage(dockerImageName)) {
            throw new IllegalArgumentException(
                    String.format("Image %s does not appear to be a valid Synthesized TDK.",
                            dockerImageName.asCanonicalNameString()));
        }

        String inputURL = convertUrl(input);
        String outputURL = convertUrl(output);
        try (GenericContainer<?> container = new GenericContainer<>(dockerImageName)) {
            if (license != null) {
                container.withEnv("SYNTHESIZED_KEY", license);
            }
            try {
                container.withNetwork(input.getNetwork())
                        .withEnv("SYNTHESIZED_INPUT_PASSWORD", input.getPassword())
                        .withEnv("SYNTHESIZED_INPUT_USERNAME", input.getUsername())
                        .withEnv("SYNTHESIZED_INPUT_URL", inputURL)
                        .withEnv("SYNTHESIZED_OUTPUT_PASSWORD", output.getPassword())
                        .withEnv("SYNTHESIZED_OUTPUT_USERNAME", output.getUsername())
                        .withEnv("SYNTHESIZED_OUTPUT_URL", outputURL)
                        .withEnv("SYNTHESIZED_USERCONFIG", config)
                        .withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy())
                        .start();
            } catch (Throwable e) {
                LOGGER.warn(container.getLogs());
                throw e;
            }
            container.followOutput(new Slf4jLogConsumer(LOGGER));
        }
    }

    static boolean validateImage(DockerImageName dockerImageName) {
        try (GenericContainer<?> container = new GenericContainer<>(dockerImageName)) {
            container.withEnv("SYNTHESIZED_COMMAND", "tdk --version")
                    .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
                    .start();
            return container.getLogs().contains("Synthesized TDK CLI ver");
        }
    }

    static String convertUrl(JdbcDatabaseContainer<?> container) {
        Objects.requireNonNull(container);
        int exposedPort = container.getExposedPorts().get(0);
        return container.getJdbcUrl()
                .replace(container.getHost(), container.getNetworkAliases().get(0))
                .replace(Integer.toString(container.getMappedPort(exposedPort)), Integer.toString(exposedPort));
    }
}
