package com.bank.testDB;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@TestConfiguration
public class LiquibaseTestConfig {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    public static void runLiquibase() throws Exception {
        if (initialized.compareAndSet(false, true)) {
            try (Connection conn = DriverManager.getConnection(
                    CommonPostgresContainer.getJdbcUrl(),
                    CommonPostgresContainer.getUsername(),
                    CommonPostgresContainer.getPassword()
            )) {
                Liquibase liquibase = new Liquibase(
                        "db/liquibase/db.changelog-master.xml",
                        new ClassLoaderResourceAccessor(),
                        new JdbcConnection(conn)
                );
                try {
                    liquibase.update(new Contexts(), new LabelExpression());
                    log.info("Liquibase-скрипты проинициализированы.");
                } catch (Exception e) {
                    log.error("Ошибка при инициализации Liquibase-скриптов: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
