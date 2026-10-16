package com.utegear.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.entity.User;
import com.utegear.entity.WishlistItem;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PostgresqlMigrationRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("utegear_test")
            .withUsername("utegear")
            .withPassword("test-only-password");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private StoreInventoryRepository inventoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WishlistItemRepository wishlistRepository;

    @BeforeEach
    void cleanData() {
        jdbc.execute("TRUNCATE TABLE wishlist_items, store_inventory, product_images, product_variants, "
                + "products, stores, categories, user_roles, users RESTART IDENTITY CASCADE");
    }

    @Test
    void flywayCreatesCoreSchemaAndPostgresqlIndexes() {
        Integer migrationCount = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class);
        Integer jsonbColumns = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND data_type = 'jsonb'
                """, Integer.class);
        Integer partialIndexes = jdbc.queryForObject("""
                SELECT count(*) FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN ('uk_addresses_one_default_per_user',
                                    'uk_product_variants_one_default',
                                    'uk_product_images_one_primary')
                """, Integer.class);

        assertThat(migrationCount).isEqualTo(3);
        assertThat(jsonbColumns).isGreaterThanOrEqualTo(3);
        assertThat(partialIndexes).isEqualTo(3);
    }

    @Test
    void productAvailabilitySupportsUnfilteredAndCaseInsensitiveProvinceQueries() {
        Product product = persistProduct();
        Store store = storeRepository.saveAndFlush(store());
        StoreInventory stock = new StoreInventory(store, product.getVariants().getFirst());
        stock.receive(4);
        inventoryRepository.saveAndFlush(stock);

        assertThat(inventoryRepository.findProductAvailability(product.getId())).hasSize(1);
        assertThat(inventoryRepository.findProductAvailability(product.getId(), store.getProvince().toLowerCase()))
                .hasSize(1);
        assertThat(inventoryRepository.findProductAvailability(product.getId(), "Other Province")).isEmpty();
    }

    @Test
    void inventoryRepositoryUsesStoreAndVariantAndDatabaseRejectsDuplicate() {
        Product product = persistProduct();
        Store store = storeRepository.saveAndFlush(store());
        ProductVariant variant = product.getVariants().getFirst();
        StoreInventory inventory = new StoreInventory(store, variant);
        inventory.receive(5);
        inventoryRepository.saveAndFlush(inventory);

        assertThat(inventoryRepository.findByStoreIdAndVariantId(store.getId(), variant.getId()))
                .get().extracting(StoreInventory::getQuantity).isEqualTo(5);

        assertThatThrownBy(() -> inventoryRepository.saveAndFlush(new StoreInventory(store, variant)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void wishlistRepositoryScopesQueriesByOwnerAndRejectsDuplicateProduct() {
        Product product = persistProduct();
        User owner = userRepository.saveAndFlush(new User(
                "Owner", "owner@example.test", null, "encoded-password"));
        User other = userRepository.saveAndFlush(new User(
                "Other", "other@example.test", null, "encoded-password"));
        wishlistRepository.saveAndFlush(new WishlistItem(owner, product));

        assertThat(wishlistRepository.findByUserIdAndProductId(owner.getId(), product.getId())).isPresent();
        assertThat(wishlistRepository.findByUserIdAndProductId(other.getId(), product.getId())).isEmpty();
        assertThatThrownBy(() -> wishlistRepository.saveAndFlush(new WishlistItem(owner, product)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Product persistProduct() {
        Category category = categoryRepository.saveAndFlush(new Category("Mouse", "mouse", null, 1));
        Product product = new Product(
                "UTE Mouse", "ute-mouse", category, null, new BigDecimal("790000"), null);
        product.addVariant(new ProductVariant("UTE-MOUSE-DEFAULT", Map.of(), true, BigDecimal.ZERO));
        return productRepository.saveAndFlush(product);
    }

    private Store store() {
        return new Store("UTEGear Thủ Đức", "01 Võ Văn Ngân", "TP.HCM", "Thủ Đức",
                null, null, null, LocalTime.of(8, 0), LocalTime.of(21, 0), null,
                null, null, true);
    }
}
