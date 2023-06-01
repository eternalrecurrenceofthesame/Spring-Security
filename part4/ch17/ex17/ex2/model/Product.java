package com.srping.aspectfilter.ex2.model;

import lombok.Data;

@Data
public class Product {

    private String name;
    private String owner; // 사용자 이름

    public Product(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }
}
