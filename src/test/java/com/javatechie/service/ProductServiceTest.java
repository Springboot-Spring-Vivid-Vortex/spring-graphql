package com.javatechie.service;

import com.javatechie.entity.Product;
import com.javatechie.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductService}. The repository is mocked so these tests
 * exercise only the service's business logic in isolation, without a Spring context
 * or a database.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(1, "Laptop", "Electronics", 999.99f, 10);
    }

    @Test
    void getProducts_returnsAllProductsFromRepository() {
        List<Product> products = List.of(product, new Product(2, "Phone", "Electronics", 499.99f, 20));
        when(repository.findAll()).thenReturn(products);

        List<Product> result = service.getProducts();

        assertThat(result).hasSize(2).containsExactlyElementsOf(products);
        verify(repository, times(1)).findAll();
    }

    @Test
    void getProducts_returnsEmptyListWhenNoProductsExist() {
        when(repository.findAll()).thenReturn(List.of());

        List<Product> result = service.getProducts();

        assertThat(result).isEmpty();
    }

    @Test
    void getProductsByCategory_returnsMatchingProducts() {
        when(repository.findByCategory("Electronics")).thenReturn(List.of(product));

        List<Product> result = service.getProductsByCategory("Electronics");

        assertThat(result).containsExactly(product);
        verify(repository).findByCategory("Electronics");
    }

    @Test
    void getProductsByCategory_returnsEmptyListWhenCategoryHasNoMatches() {
        when(repository.findByCategory("Unknown")).thenReturn(List.of());

        List<Product> result = service.getProductsByCategory("Unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void updateStock_whenProductExists_updatesAndReturnsProduct() {
        when(repository.findById(1)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.updateStock(1, 50);

        assertThat(result.getStock()).isEqualTo(50);
        verify(repository).save(product);
    }

    @Test
    void updateStock_whenProductNotFound_throwsRuntimeException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStock(99, 50))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("product not found with id 99");

        verify(repository, never()).save(any());
    }

    @Test
    void receiveNewShipment_whenProductExists_incrementsStockAndReturnsProduct() {
        when(repository.findById(1)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.receiveNewShipment(1, 5);

        assertThat(result.getStock()).isEqualTo(15);
        verify(repository).save(product);
    }

    @Test
    void receiveNewShipment_whenProductNotFound_throwsRuntimeException() {
        when(repository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.receiveNewShipment(42, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("product not found with id 42");

        verify(repository, never()).save(any());
    }
}
