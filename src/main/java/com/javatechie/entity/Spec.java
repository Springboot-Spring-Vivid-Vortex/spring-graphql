package com.javatechie.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Middle level of the Product -> spec -> insideSpec nesting (see schema.graphqls).
 * Not a JPA @Entity - it's a plain value object nested inside {@link Product#getSpec()},
 * carrying its own nested list of {@link InsideSpec}, and persisted as JSON via
 * SpecListConverter.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spec {

    private List<InsideSpec> insideSpec = new ArrayList<>();
}
