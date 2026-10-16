package com.utegear.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.utegear.entity.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:showroom_repository;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShowroomInventoryRepositoryTest {
    @Autowired ProductRepository products;
    @Autowired StoreRepository stores;
    @Autowired StoreInventoryRepository inventories;
    @Autowired CategoryRepository categories;
    @Autowired EntityManager entityManager;

    @Test
    void filtersByStoreStockAndVisibilityDeduplicatesProductsAndLoadsBothCollections() {
        var branch = stores.save(store("Selected", true));
        var other = stores.save(store("Other", true));
        var inactive = stores.save(store("Inactive", false));
        var category = categories.save(new Category("Mouse", "showroom-mouse", null, 1));
        var visible = product("visible", category);
        visible.addVariant(new ProductVariant("visible-second", Map.of(), false, BigDecimal.ZERO));
        visible.addImage(new ProductImage("https://res.cloudinary.com/demo/image.png", "showroom-test-image", "Image", 0, true));
        products.save(visible);
        var foreign = products.save(product("other-store", category));
        var zero = products.save(product("zero", category));
        var disabled = product("disabled", category);
        disabled.changeStatus(ProductStatus.INACTIVE);
        products.save(disabled);
        stock(branch, visible.getVariants().get(0), 10);
        stock(branch, visible.getVariants().get(1), 3);
        stock(other, visible.getVariants().getFirst(), 99);
        stock(other, foreign.getVariants().getFirst(), 10);
        stock(branch, zero.getVariants().getFirst(), 0);
        stock(branch, disabled.getVariants().getFirst(), 10);
        entityManager.flush();
        entityManager.clear();

        var selected = products.findInStockAtStore(branch.getId(), ProductStatus.ACTIVE);
        assertThat(selected).extracting(Product::getId).containsExactly(visible.getId());
        assertThat(selected.getFirst().getVariants()).hasSize(2);
        products.findWithImagesByIds(List.of(visible.getId()));
        assertThat(selected.getFirst().getImages()).hasSize(1);
        assertThat(inventories.sumStoreStockByVariantIds(branch.getId(), visible.getVariants().stream().map(ProductVariant::getId).toList()))
                .extracting(StoreInventoryRepository.VariantStock::getQuantity).containsExactlyInAnyOrder(10L, 3L);
        assertThat(products.findInStockAtStore(inactive.getId(), ProductStatus.ACTIVE)).isEmpty();
    }

    private Product product(String slug, Category category) {
        var product = new Product(slug, slug, category, null, new BigDecimal("100000"), null);
        product.addVariant(new ProductVariant(slug + "-default", Map.of(), true, BigDecimal.ZERO));
        return product;
    }
    private Store store(String name, boolean active) {
        return new Store(name, "Demo", "Demo", null, null, null, null,
                LocalTime.of(8, 0), LocalTime.of(22, 0), null, null, null, active);
    }
    private void stock(Store store, ProductVariant variant, int quantity) {
        var inventory = new StoreInventory(store, variant);
        if (quantity > 0) inventory.receive(quantity);
        inventories.save(inventory);
    }
}
