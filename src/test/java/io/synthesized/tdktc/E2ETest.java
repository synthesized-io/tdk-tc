package io.synthesized.tdktc;

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
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class E2ETest {
    private Network network;
    private PostgreSQLContainer<?> input;
    private PostgreSQLContainer<?> output;

    //tag::containers[]
    @BeforeEach
    void setUp() {
        network = Network.newNetwork();
        input = getContainer("input", true);
        output = getContainer("output", false);
        input.start();
        output.start();
    }

    private PostgreSQLContainer<?> getContainer(String name, boolean init) {
        PostgreSQLContainer<?> result = new PostgreSQLContainer<>("postgres:11.1")
                .withDatabaseName(name)
                .withUsername("user")
                .withPassword("password")
                .withNetwork(network);
        result = init ? result.withInitScript("dbcreate.sql") : result;
        return result;
    }
    //end::containers[]

    @AfterEach
    void tearDown() {
        input.stop();
        output.stop();
    }

    @Test
    void e2eTest() throws SQLException {
        // tag::transform[]
        new SynthesizedTDK(SynthesizedTDK.DEFAULT_IMAGE_NAME)
          // Use this method to alter container image name for the TDK-CLI container
          //.setImageName(...)

          // Use this method to set license key in case you are using paid version of TDK-CLI
          //.setLicense(...)
                .transform(
                    //Input JdbcDatabaseContainer: empty database with schema
                    input, 
                    //Output JdbcDatabaseContainer: output database with generated data
                    output,
                        "default_config:\n" +
                                "    mode: \"GENERATION\"\n" +
                                "    target_row_number: 10\n" +
                                "global_seed: 42\n"
                );
        // end::transform[]
        try (Connection conn = DriverManager.getConnection(
                output.getJdbcUrl(), output.getUsername(), output.getPassword());
             Statement stmt = conn.createStatement()) {
            for (String tableName :
                    Arrays.asList("speaker", "conference", "talk", "talkspeakers")) {
                try (ResultSet resultSet = stmt.executeQuery("select count(*) from " + tableName)) {
                    resultSet.next();
                    assertThat(resultSet.getInt(1)).isEqualTo(10);
                }
            }
        }
    }
}
