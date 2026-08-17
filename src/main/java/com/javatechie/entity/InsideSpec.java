package com.javatechie.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Innermost level of the Product -> spec -> insideSpec nesting (see schema.graphqls).
 * Not a JPA @Entity - it's a plain value object nested inside {@link Spec}, itself nested
 * inside {@link Product#getSpec()}, and persisted as JSON via SpecListConverter.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsideSpec {

    private String field;
}
