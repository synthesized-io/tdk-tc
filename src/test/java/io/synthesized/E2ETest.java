package io.synthesized;

import io.synthesized.tdktc.SynthesizedTDK;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class E2ETest {
    private Network network;
    private PostgreSQLContainer<?> input;
    private PostgreSQLContainer<?> output;

    @BeforeEach
    void setUp() {
        network = Network.newNetwork();
        input = getContainer("input", true);
        output = getContainer("output", false);
        input.start();
        output.start();
    }

    @AfterEach
    void tearDown() {
        input.stop();
        output.stop();
    }

    private PostgreSQLContainer<?> getContainer(String name, boolean init) {
        var result = new PostgreSQLContainer<>("postgres:11.1")
                .withDatabaseName(name)
                .withUsername("user")
                .withPassword("password")
                .withNetwork(network);
        result = init ? result.withInitScript("dbcreate.sql") : result;
        return result;
    }

    @Test
    void e2eTest() throws SQLException {
        new SynthesizedTDK()
                .setImageName("sscli")
                .transform(input, output,
                        "default_config:\n" +
                                "    mode: \"GENERATION\"\n" +
                                "    target_row_number: 10\n" +
                                "global_seed: 42\n"
                );
        try (Connection conn = DriverManager.getConnection(
                output.getJdbcUrl(), output.getUsername(), output.getPassword());
             Statement stmt = conn.createStatement()) {
            for (String tableName : List.of("speaker", "conference", "talk", "talkspeakers")) {
                try (ResultSet resultSet = stmt.executeQuery("select count(*) from " + tableName)) {
                    resultSet.next();
                    Assertions.assertThat(resultSet.getInt(1)).isEqualTo(10);
                }
            }
        }
    }
}