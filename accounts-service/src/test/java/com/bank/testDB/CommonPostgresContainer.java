package com.bank.testDB;

import org.testcontainers.containers.PostgreSQLContainer;

public class CommonPostgresContainer {

    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testDB")
            .withUsername("test")
            .withPassword("test")
            .withCreateContainerCmdModifier(cmd -> cmd.withName("test-postgres-container"));

    static {
        POSTGRES.start();
    }

    public static String getR2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%d/%s",
                POSTGRES.getHost(),
                POSTGRES.getFirstMappedPort(),
                POSTGRES.getDatabaseName());
    }

    public static String getJdbcUrl() {
        return POSTGRES.getJdbcUrl();
    }

    public static String getUsername() {
        return POSTGRES.getUsername();
    }

    public static String getPassword() {
        return POSTGRES.getPassword();
    }
}
