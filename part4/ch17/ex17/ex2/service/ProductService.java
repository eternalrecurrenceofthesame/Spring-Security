package com.srping.aspectfilter.ex2.service;

import com.srping.aspectfilter.ex2.model.Product;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    /**
     * 간단한 예제 진행을 위해 컬렉션 데이터를 만들었다.
     * 필터링 규칙을 준수하는 값만 컬렉션에 포함된다.
     * @return
     */
    @PostFilter("filterObject.owner == authentication.name")
    public List<Product> findProducts(){
        List<Product> products = new ArrayList<>();

        products.add(new Product("beer", "nikolai"));
        products.add(new Product("candy", "nikolai"));
        products.add(new Product("chocolate", "julien"));

        return products;
    }
}
