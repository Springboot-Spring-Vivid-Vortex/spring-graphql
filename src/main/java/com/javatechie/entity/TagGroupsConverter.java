package com.javatechie.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

/**
 * JPA cannot map a nested collection (List&lt;List&lt;String&gt;&gt;) directly to a column, since
 * {@code @ElementCollection} only supports a single level of basic/embeddable types. This converter
 * serializes {@link Product#getTagGroups()} to/from a JSON string so it can be stored in a single
 * TEXT column, on both H2 (tests) and MySQL (runtime).
 */
@Converter
public class TagGroupsConverter implements AttributeConverter<List<List<String>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<List<String>>> TAG_GROUPS_TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<List<String>> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? Collections.emptyList() : attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize tagGroups to JSON", e);
        }
    }

    @Override
    public List<List<String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(dbData, TAG_GROUPS_TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to deserialize tagGroups from JSON", e);
        }
    }
}
