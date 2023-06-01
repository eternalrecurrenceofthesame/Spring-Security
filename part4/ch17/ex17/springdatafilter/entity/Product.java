package com.srping.aspectfilter.springdatafilter.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@NoArgsConstructor
@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String owner;

    public Product(int id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
}
