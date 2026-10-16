package com.utegear.demo;

import static org.assertj.core.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class DemoBranchInventoryTest {
    JdbcTemplate jdbc;
    @BeforeEach
    void setup() {
        jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE stores(id bigint PRIMARY KEY, active boolean, email varchar)");
        jdbc.execute("CREATE TABLE products(id bigint PRIMARY KEY, status varchar)");
        jdbc.execute("CREATE TABLE product_variants(id bigint PRIMARY KEY, product_id bigint, active boolean)");
        jdbc.execute("CREATE TABLE store_inventory(store_id bigint, variant_id bigint, quantity int, updated_at timestamp, PRIMARY KEY(store_id,variant_id))");
        jdbc.update("INSERT INTO stores VALUES (1,true,null),(2,true,'hanoi@demo.utegear.test'),(3,true,'danang@demo.utegear.test')");
        jdbc.update("INSERT INTO products VALUES (1,'ACTIVE'),(2,'INACTIVE')");
        jdbc.update("INSERT INTO product_variants VALUES (11,1,true),(12,1,true),(13,2,true),(14,1,false)");
        jdbc.update("INSERT INTO store_inventory(store_id,variant_id,quantity) VALUES (1,11,9),(1,12,4),(1,13,10),(1,14,10)");
    }

    @Test
    void addsTenForEachActiveSourceVariantToOnlyTheTwoNewStores() {
        var result = DemoBranchInventory.provision(jdbc,10);
        assertThat(result).allMatch(store -> store.insertedVariants()==2 && store.totalQuantity()==20);
        assertThat(jdbc.queryForObject("SELECT sum(quantity) FROM store_inventory WHERE store_id=1", Long.class)).isEqualTo(33);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM store_inventory WHERE store_id IN (2,3) AND variant_id IN (13,14)", Long.class)).isZero();
    }

    @Test
    void rerunPreservesSoldAndOutOfStockQuantities() {
        DemoBranchInventory.provision(jdbc,10);
        jdbc.update("UPDATE store_inventory SET quantity=0 WHERE store_id=2 AND variant_id=11");
        var result = DemoBranchInventory.provision(jdbc,10);
        assertThat(result).allMatch(store -> store.insertedVariants()==0);
        assertThat(jdbc.queryForObject("SELECT quantity FROM store_inventory WHERE store_id=2 AND variant_id=11", Integer.class)).isZero();
    }

    @Test
    void rejectsAnUnexpectedTargetStoreBeforeWriting() {
        jdbc.update("UPDATE stores SET email='other@example.test' WHERE id=3");
        assertThatThrownBy(() -> DemoBranchInventory.provision(jdbc,10)).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM store_inventory WHERE store_id<>1", Long.class)).isZero();
    }
}
