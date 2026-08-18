package com.javatechie.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Map;

/**
 * Maps service-layer exceptions to GraphQL errors.
 *
 * Without this, an uncaught RuntimeException reaches the client as a generic error with
 * classification INTERNAL_ERROR and its message hidden (Spring GraphQL does this by
 * default, so internal exception details aren't leaked). These handlers give
 * ProductNotFoundException and InvalidProductDataException a specific error classification,
 * their real message, and a small set of extensions for client-side handling/logging.
 *
 * @GraphQlExceptionHandler methods are picked up from any @Controller or, as here,
 * @ControllerAdvice class - so this applies to exceptions thrown by every GraphQL
 * controller in the app, not just ProductController.
 */
@ControllerAdvice
public class GraphQLExceptionHandler {

    // Scenario 1: Production Error with Custom Extensions
    @GraphQlExceptionHandler
    public GraphQLError handle(ProductNotFoundException ex, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getMessage())
                .extensions(Map.of(
                        "errorCode", "PRODUCT_NOT_FOUND",
                        "timestamp", System.currentTimeMillis()
                ))
                .build();
    }

    // Scenario 2: Execution Error with Partial Data (Server-Side Fault)
    @GraphQlExceptionHandler
    public GraphQLError handle(InvalidProductDataException ex, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .extensions(Map.of(
                        "errorCode", "INVALID_INPUT_DATA",
                        "timestamp", System.currentTimeMillis()
                ))
                .build();
    }
}
