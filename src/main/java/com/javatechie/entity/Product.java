package com.javatechie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String category;
    private Float price;
    private Integer stock;

    // A list of lists (e.g. grouped tags: [["wireless","bluetooth"],["clearance"]]).
    // Stored as JSON via TagGroupsConverter since JPA's @ElementCollection only supports
    // a single level of nesting, not a collection of collections.
    @Convert(converter = TagGroupsConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<List<String>> tagGroups = new ArrayList<>();

    public Product(String name, String category, Float price, Integer stock) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.tagGroups = new ArrayList<>();
    }

    public Product(Integer id, String name, String category, Float price, Integer stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.tagGroups = new ArrayList<>();
    }
}
