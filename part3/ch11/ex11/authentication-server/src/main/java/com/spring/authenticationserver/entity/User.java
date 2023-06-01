package com.spring.authenticationserver.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 사용자 자격 증명(로그인) 정보를 저장하는 테이블
 */
@Data
@Table(name = "spring_user")
@Entity
public class User {

    @Id
    private String username;

    private String password;


}
