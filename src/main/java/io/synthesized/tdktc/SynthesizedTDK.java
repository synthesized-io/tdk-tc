package io.synthesized.tdktc;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;

public final class SynthesizedTDK {
    private final String license;

    public SynthesizedTDK(String license) {
        this.license = license;
    }


    public void transform(
            JdbcDatabaseContainer<?> input,
            JdbcDatabaseContainer<?> output,
            String config
    ) {
        String inputURL = convertUrl(input);
        String outputURL = convertUrl(output);
        GenericContainer<?> foo = new GenericContainer<>("synthesized-cli")
                .withNetwork(input.getNetwork())
                .withEnv("SYNTHESIZED_KEY", license)
                .withEnv("SYNTHESIZED_INPUT_PASSWORD", input.getPassword())
                .withEnv("SYNTHESIZED_INPUT_USERNAME", input.getUsername())
                .withEnv("SYNTHESIZED_INPUT_URL", inputURL)
                .withEnv("SYNTHESIZED_OUTPUT_PASSWORD", output.getPassword())
                .withEnv("SYNTHESIZED_OUTPUT_USERNAME", output.getUsername())
                .withEnv("SYNTHESIZED_OUTPUT_URL", outputURL)
                .withEnv("SYNTHESIZED_USERCONFIG", config)
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy());
        foo.start();
        foo.followOutput(of -> System.out.print("synthesized:" + of.getUtf8String()));
        System.out.println("DONE");
    }

    static String convertUrl(JdbcDatabaseContainer<?> container) {
        int exposedPort = container.getExposedPorts().get(0);
        return container.getJdbcUrl().replace("localhost",
                container.getNetworkAliases().get(0)).replace(
                Integer.toString(container.getMappedPort(exposedPort)),
                Integer.toString(exposedPort));
    }
}
