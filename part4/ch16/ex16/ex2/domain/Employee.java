package com.spring.securityaspect.ex2.domain;

import lombok.Data;

import java.util.List;

@Data
public class Employee {

    private String name;
    private List<String> books;
    private List<String> roles; // 역할 정의(할 수 있는 것)

    public Employee(String name, List<String> books, List<String> roles) {

        this.name = name;
        this.books = books;
        this.roles = roles;
    }
}
