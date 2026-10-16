package com.utegear.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("local & !prod")
@ConditionalOnProperty(name = "app.demo.setup", havingValue = "true")
public class DemoBranchSetup {
    @Bean
    CommandLineRunner provisionDemoBranches(JdbcTemplate jdbc, PasswordEncoder encoder,
            TransactionTemplate transactions, ObjectMapper json, Environment environment) {
        return args -> {
            if (!"development".equals(environment.getProperty("app.demo.environment"))) {
                throw new IllegalStateException("Explicit --app.demo.environment=development confirmation is required");
            }
            Path directory = Path.of("crawler/data/output/demo-accounts").toAbsolutePath();
            Files.createDirectories(directory);
            Path report = directory.resolve("staff-" + UUID.randomUUID() + ".json");
            try {
                transactions.executeWithoutResult(status -> {
                    jdbc.execute("LOCK TABLE stores, users IN SHARE ROW EXCLUSIVE MODE");
                    var result = new DemoBranchProvisioner(jdbc, encoder).provision();
                    try {
                        Files.writeString(report, json.writerWithDefaultPrettyPrinter().writeValueAsString(result),
                                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    } catch (Exception failure) {
                        throw new IllegalStateException("Cannot export account credentials; transaction will roll back", failure);
                    }
                });
            } catch (Exception failure) {
                Files.deleteIfExists(report);
                throw failure;
            }
            System.out.println("Demo branches ready. Private account report: " + report);
        };
    }
}
