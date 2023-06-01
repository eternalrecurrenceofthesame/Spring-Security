package com.securty.csrfex3.entity;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity
@Table(name="spring_token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String identifier; // 사용자 로그인 세션값
    private String token;
}
