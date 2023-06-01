package com.srping.aspectfilter.springdatafilter.controller;

import com.srping.aspectfilter.springdatafilter.entity.Product;
import com.srping.aspectfilter.springdatafilter.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class ProductController {

    /**
     * 간단한 조회 엔드포인트이기 떄문에 바로 데이터 호출
     */
    private ProductRepository productRepository;

    @GetMapping("/products/{text}")
    public List<Product> findProductsContaining(@PathVariable String text){
        return productRepository.findProductByNameContains(text);
    }

}
