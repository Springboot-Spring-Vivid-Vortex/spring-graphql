package com.javatechie.resource;

import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration tests for the GraphQL endpoints exposed by {@link ProductController}.
 * Boots the entire Spring context on a random port, talks to the real HTTP /graphql endpoint via
 * {@link HttpGraphQlTester}, and persists to an in-memory H2 database (see
 * src/test/resources/application.properties), exercising controller, service and repository
 * together.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
class ProductControllerIntegrationTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private ProductRepository repository;

    private Product laptop;
    private Product phone;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        laptop = repository.save(new Product("Laptop", "Electronics", 999.99f, 10));
        phone = repository.save(new Product("Phone", "Electronics", 499.99f, 20));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void getProducts_returnsAllProductsFromDatabase() {
        String query = """
                query {
                    getProducts {
                        name
                        category
                        price
                        stock
                    }
                }
                """;

        List<String> names = graphQlTester.document(query)
                .execute()
                .path("getProducts[*].name")
                .entityList(String.class)
                .get();

        assertThat(names).containsExactlyInAnyOrder("Laptop", "Phone");
    }

    @Test
    void getProducts_returnsEmptyListWhenNoProductsExist() {
        repository.deleteAll();

        String query = """
                query {
                    getProducts {
                        name
                    }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("getProducts")
                .entityList(Object.class)
                .hasSize(0);
    }

    @Test
    void getProductsByCategory_returnsOnlyMatchingProducts() {
        repository.save(new Product("Desk", "Furniture", 150.0f, 5));

        String query = """
                query {
                    getProductsByCategory(category: "Electronics") {
                        name
                        category
                    }
                }
                """;

        List<String> categories = graphQlTester.document(query)
                .execute()
                .path("getProductsByCategory[*].category")
                .entityList(String.class)
                .get();

        assertThat(categories).hasSize(2).allMatch("Electronics"::equals);
    }

    @Test
    void getProductsByCategory_returnsEmptyListForUnknownCategory() {
        String query = """
                query {
                    getProductsByCategory(category: "Unknown") {
                        name
                    }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .path("getProductsByCategory")
                .entityList(Object.class)
                .hasSize(0);
    }

    @Test
    void updateStock_persistsNewStockAndReturnsUpdatedProduct() {
        String mutation = """
                mutation {
                    updateStock(id: %d, stock: 100) {
                        name
                        stock
                    }
                }
                """.formatted(laptop.getId());

        graphQlTester.document(mutation)
                .execute()
                .path("updateStock.stock")
                .entity(Integer.class)
                .isEqualTo(100);

        Product updated = repository.findById(laptop.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(100);
    }

    @Test
    void updateStock_withUnknownId_returnsGraphQlError() {
        String mutation = """
                mutation {
                    updateStock(id: 999999, stock: 5) {
                        stock
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void receiveNewShipment_incrementsExistingStock() {
        String mutation = """
                mutation {
                    receiveNewShipment(id: %d, quantity: 15) {
                        name
                        stock
                    }
                }
                """.formatted(phone.getId());

        graphQlTester.document(mutation)
                .execute()
                .path("receiveNewShipment.stock")
                .entity(Integer.class)
                .isEqualTo(35);

        Product updated = repository.findById(phone.getId()).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(35);
    }

    @Test
    void receiveNewShipment_withUnknownId_returnsGraphQlError() {
        String mutation = """
                mutation {
                    receiveNewShipment(id: 999999, quantity: 5) {
                        stock
                    }
                }
                """;

        graphQlTester.document(mutation)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
