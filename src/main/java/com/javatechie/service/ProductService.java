package com.javatechie.service;

import com.javatechie.entity.Product;
import com.javatechie.exception.InvalidProductDataException;
import com.javatechie.exception.ProductNotFoundException;
import com.javatechie.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public List<Product> getProducts(){
        return repository.findAll();
    }

    public List<Product> getProductsByCategory(String category){
        return repository.findByCategory(category);
    }

    // Scenario 1: Production Error with Custom Extensions
    // ProductNotFoundException is mapped to a NOT_FOUND GraphQL error with custom
    // extensions by GraphQLExceptionHandler - see that class for the actual mapping.
    public Product getProductById(int id){
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    // Scenario 2: Execution Error with Partial Data (Server-Side Fault)
    // Invalid input (negative stock) or a missing product both throw here; either way
    // GraphQLExceptionHandler turns it into a BAD_REQUEST/NOT_FOUND error, so this field
    // resolves to null while any other fields in the same request are unaffected.
    public Product updateStock(int id, int stock){
        if (stock < 0) {
            throw new InvalidProductDataException("Stock cannot be negative. Provided value: " + stock);
        }

        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        existingProduct.setStock(stock);
        return repository.save(existingProduct);
    }

    // Scenario 2: Execution Error with Partial Data (Server-Side Fault)
    // Same as updateStock: invalid quantity or a missing product throws.
    public Product receiveNewShipment(int id, int quantity){
        if (quantity <= 0) {
            throw new InvalidProductDataException("Shipment quantity must be positive. Provided value: " + quantity);
        }

        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        existingProduct.setStock(existingProduct.getStock() + quantity);
        return repository.save(existingProduct);
    }

    // Scenario 3: Syntax / Validation Error (Client-Side Fault)
    // This one never reaches the service layer - a query with a missing required
    // argument or a wrong argument type (e.g. getProductById(id: "abc")) is rejected by
    // GraphQL's own validation before any resolver runs, so no exception class is needed.
}
