package com.javatechie.resource;

import com.javatechie.entity.Product;
import com.javatechie.entity.Spec;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates @BatchMapping, Spring GraphQL's resolver-level batching. When a query asks
 * for a field across a list of parents (e.g. Product.spec for every product returned by
 * getProducts), Spring normally resolves that field once per parent. @BatchMapping instead
 * hands you the whole list of parents in one call, so you can fetch their child data with a
 * single query - e.g. "SELECT * FROM spec WHERE product_id IN (...)" instead of one query
 * per product. That's what avoids the classic N+1 problem.
 *
 * Caveat specific to this project: Product.spec and Product.tagGroups are stored as JSON on
 * the product row itself (see SpecListConverter / TagGroupsConverter), not in a separate
 * table. That means they're already loaded the moment a Product is fetched - there's no
 * extra query to batch away for these two fields. The method below is functionally a no-op
 * (it just returns data Product already has), kept only to show the @BatchMapping method
 * shape. It would earn its keep if Spec ever moved into its own table/repository, looked up
 * by product id.
 *
 * Requires Spring Boot 3.0+ / Spring GraphQL 1.0+ (this project is on Spring Boot 3.3.0).
 *
 * Note: a field can only have one resolver. Don't also add @QueryMapping/@SchemaMapping
 * for "Product.spec" elsewhere, or Spring GraphQL will fail to start with an ambiguous
 * mapping error.
 */
@Controller
public class ProductSpecResolver {

    @BatchMapping(typeName = "Product", field = "spec")
    public Map<Product, List<Spec>> loadSpecsByProductIds(List<Product> products) {
        // In a schema where Spec lived in its own table, this is where you'd run one
        // query for all product ids and group the results, e.g.:
        //   Map<Integer, List<Spec>> byProductId = specRepository.findByProductIdIn(ids)...
        // Here, Product already carries its own spec list, so we just return it as-is.
        return products.stream().collect(Collectors.toMap(p -> p, Product::getSpec));
    }
}
