package com.spring.authenticationserver.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 인증되 사용자의 생성된 OTP 를 저장하는 테이블
 */
@Data
@Entity
@Table(name = "spring_otp")
public class Otp {

    @Id
    private String username;

    private String code; // otp

}
