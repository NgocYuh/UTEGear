package com.utegear.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("local & !prod")
@ConditionalOnProperty(name = "app.demo.inventory-setup", havingValue = "true")
public class DemoBranchInventorySetup {
    @Bean
    CommandLineRunner provisionDemoInventory(JdbcTemplate jdbc, TransactionTemplate transactions, Environment environment) {
        return args -> {
            if (!"development".equals(environment.getProperty("app.demo.environment"))) {
                throw new IllegalStateException("Explicit development environment confirmation is required");
            }
            var result = transactions.execute(status -> {
                jdbc.execute("LOCK TABLE store_inventory IN SHARE ROW EXCLUSIVE MODE");
                return DemoBranchInventory.provision(jdbc, 10);
            });
            result.forEach(branch -> System.out.printf("CN-%d: inserted %d variants; total %d variants, %d units%n",
                    branch.storeId(), branch.insertedVariants(), branch.totalVariants(), branch.totalQuantity()));
        };
    }
}
