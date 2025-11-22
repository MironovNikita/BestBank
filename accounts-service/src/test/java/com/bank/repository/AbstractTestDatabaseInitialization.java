package com.bank.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.bank.testDB.CommonPostgresContainer;
import com.bank.testDB.LiquibaseTestConfig;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTestDatabaseInitialization {

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", CommonPostgresContainer::getR2dbcUrl);
        registry.add("spring.r2dbc.username", CommonPostgresContainer::getUsername);
        registry.add("spring.r2dbc.password", CommonPostgresContainer::getPassword);
    }

    @BeforeAll
    void initializeLiquibase() throws Exception {
        LiquibaseTestConfig.runLiquibase();
    }
}
