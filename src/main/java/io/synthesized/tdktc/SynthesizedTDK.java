package io.synthesized.tdktc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;

public final class SynthesizedTDK {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynthesizedTDK.class);

    private String license;
    private String imageName = "synthesizedio/synthesized-tdk-cli:latest";

    /**
     * Sets license key for paid version of TDK.
     *
     * @param license License key.
     */
    public SynthesizedTDK setLicense(String license) {
        this.license = license;
        return this;
    }

    /**
     * Sets Docker image name which is used for running TDK.
     *
     * @param imageName image name to be used for running TDK.
     */
    public SynthesizedTDK setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public void transform(JdbcDatabaseContainer<?> input, JdbcDatabaseContainer<?> output, String config) {
        String inputURL = convertUrl(input);
        String outputURL = convertUrl(output);
        try (GenericContainer<?> container = new GenericContainer<>(imageName)) {
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
                        .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
                        .start();
            } catch (Throwable e) {
                LOGGER.warn(container.getLogs());
                throw e;
            }
            container.followOutput(new Slf4jLogConsumer(LOGGER));
        }
    }

    static String convertUrl(JdbcDatabaseContainer<?> container) {
        int exposedPort = container.getExposedPorts().get(0);
        return container.getJdbcUrl()
                .replace(container.getHost(), container.getNetworkAliases().get(0))
                .replace(Integer.toString(container.getMappedPort(exposedPort)), Integer.toString(exposedPort));
    }
}
