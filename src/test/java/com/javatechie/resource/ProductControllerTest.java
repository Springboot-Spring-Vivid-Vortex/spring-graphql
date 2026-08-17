package com.javatechie.resource;

import com.javatechie.entity.Product;
import com.javatechie.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductController}. The service is mocked and the controller
 * is instantiated directly (field injection wired via reflection), so these tests run
 * as plain POJO tests without starting a Spring or GraphQL context.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController();
        ReflectionTestUtils.setField(controller, "service", service);
    }

    @Test
    void getProducts_delegatesToServiceAndReturnsResult() {
        Product product = new Product(1, "Laptop", "Electronics", 999.99f, 10);
        product.setTagGroups(List.of(
                List.of("wireless", "bluetooth"),
                List.of("clearance")
        ));
        when(service.getProducts()).thenReturn(List.of(product));

        List<Product> result = controller.getProducts();

        assertThat(result).containsExactly(product);
        assertThat(result.get(0).getTagGroups()).containsExactly(
                List.of("wireless", "bluetooth"),
                List.of("clearance")
        );
        verify(service).getProducts();
    }

    @Test
    void getProductsByCategory_delegatesToServiceWithGivenCategory() {
        Product product = new Product(2, "Phone", "Electronics", 499.99f, 20);
        when(service.getProductsByCategory("Electronics")).thenReturn(List.of(product));

        List<Product> result = controller.getProductsByCategory("Electronics");

        assertThat(result).containsExactly(product);
        verify(service).getProductsByCategory("Electronics");
    }

    @Test
    void updateStock_delegatesToServiceWithGivenArguments() {
        Product product = new Product(1, "Laptop", "Electronics", 999.99f, 50);
        when(service.updateStock(1, 50)).thenReturn(product);

        Product result = controller.updateStock(1, 50);

        assertThat(result).isEqualTo(product);
        verify(service).updateStock(1, 50);
    }

    @Test
    void receiveNewShipment_delegatesToServiceWithGivenArguments() {
        Product product = new Product(1, "Laptop", "Electronics", 999.99f, 15);
        when(service.receiveNewShipment(1, 5)).thenReturn(product);

        Product result = controller.receiveNewShipment(1, 5);

        assertThat(result).isEqualTo(product);
        verify(service).receiveNewShipment(1, 5);
    }
}
