package com.javatechie.resource;

import com.javatechie.entity.Product;
import com.javatechie.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * HTTP request batching (multiple GraphQL operations in one HTTP call) needs no special
 * server-side support - a client can already batch any of the fields below by POSTing an
 * array instead of a single object, e.g.:
 *
 *   [
 *     { "query": "{ getProductById(id: 1) { id name } }" },
 *     { "query": "{ getProductById(id: 2) { id name } }" }
 *   ]
 *
 * Spring GraphQL runs each entry and returns the results in the same order, as a JSON
 * array. No "ForBatch" twin of a query/mutation is required - see
 * ProductSpecResolver for the other kind of batching (@BatchMapping), which batches
 * database calls made while resolving a single query's nested fields.
 */
@Controller
public class ProductController {

    @Autowired
    private ProductService service;

    @QueryMapping
    public List<Product> getProducts() {
        return service.getProducts();
    }

    @QueryMapping
    public List<Product> getProductsByCategory(@Argument String category) {
        return service.getProductsByCategory(category);
    }

    // Scenario 1: Production Error with Custom Extensions
    // Throws ProductNotFoundException when the id doesn't exist; Spring GraphQL turns it
    // into a GraphQL error entry instead of an HTTP error, with `data.getProductById: null`.
    @QueryMapping
    public Product getProductById(@Argument int id) {
        return service.getProductById(id);
    }

    // Scenario 2: Execution Error with Partial Data (Server-Side Fault)
    // Validation (negative stock) or a missing product both throw, so this mutation's
    // result is null and the error appears in the response's `errors` array - any other
    // fields queried alongside it in the same request are unaffected.
    @MutationMapping
    public Product updateStock(@Argument int id, @Argument int stock) {
        return service.updateStock(id, stock);
    }

    // Scenario 2: Execution Error with Partial Data (Server-Side Fault)
    // Same idea as updateStock - invalid quantity or missing product throws, and the
    // failure is scoped to this one field.
    @MutationMapping
    public Product receiveNewShipment(@Argument int id, @Argument int quantity) {
        return service.receiveNewShipment(id, quantity);
    }
}
