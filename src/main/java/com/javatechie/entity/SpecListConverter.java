package com.javatechie.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

/**
 * Same rationale as {@link TagGroupsConverter}: JPA has no direct mapping for a list of
 * objects that themselves contain a nested list ({@link Product#getSpec()} is
 * List&lt;Spec&gt;, where Spec.insideSpec is List&lt;InsideSpec&gt;). This converter
 * serializes the whole spec graph to/from a single JSON TEXT column, on both H2 (tests)
 * and MySQL (runtime).
 */
@Converter
public class SpecListConverter implements AttributeConverter<List<Spec>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Spec>> SPEC_LIST_TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<Spec> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? Collections.emptyList() : attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize spec to JSON", e);
        }
    }

    @Override
    public List<Spec> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(dbData, SPEC_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to deserialize spec from JSON", e);
        }
    }
}
