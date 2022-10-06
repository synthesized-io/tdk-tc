# TDK-TC

[![Actions Status: build](https://github.com/synthesized-io/tdk-tc/workflows/build/badge.svg)](https://github.com/synthesized-io/tdk-tc/actions?query=workflow%3A"build")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.synthesized/tdk-tc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.synthesized/tdk-tc)

## Synthesized TDK-Testcontainers integration

This project is a thin client for [Synthesized](https://www.synthesized.io/) [TDK](https://docs.synthesized.io/tdk/latest/) integrated with [TestContainers](https://www.testcontainers.org/) which can be used in order to generate testing data in an empty database.

## Usage

Use Maven or Gradle to import the [most recent version](https://maven-badges.herokuapp.com/maven-central/io.synthesized/tdk-tc) of TDK-TC.


```java
   new SynthesizedTDK()
      // Use this method to alter container image name for the TDK-CLI container  
      //.setImageName(...) 
      
      // Use this method to set license key in case you are using paid version of TDK-CLI
      //.setLicense(...)
      
      .transform(
         //Input JdbcDatabaseContainer: empty database with schema 
         input, 
         //Output JdbcDatabaseContainer: output database with generated data
         output,
         //Workflow configuration in YAML format
            """
            default_config:
              mode: "GENERATION"
              target_row_number: 10
            global_seed: 42
            """
    );
```

See full documentation on workflow configuration in YAML format [here](https://docs.synthesized.io/tdk/latest/user_guide/userconfig).
