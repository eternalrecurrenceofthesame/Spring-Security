package com.srping.aspectfilter.ex1.service;

import com.srping.aspectfilter.ex1.model.Product;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class ProductService {

    /**
     * 보안 컨텍스트에서 인증된 Authentication 을 가져와서 필터에 사용한다. 472 p
     *
     * products == filterObject
     */
    @PreFilter("filterObject.owner == authentication.name")
    public List<Product> sellProducts(List<Product> products){
        // 상품을 판매하고 판매된 상품의 목록을 반환한다.
        return products;
    }
}
