package com.spring.securityaspect.ex3.model;

import lombok.Data;

/**
 * 데이터 베이스에 저장되는 문서 엔티티를 가정한다.
 */
@Data
public class Document {

    private String owner;

    public Document(String owner) {
        this.owner = owner;
    }
}
