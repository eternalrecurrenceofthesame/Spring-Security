package com.srping.aspectfilter.ex2.controller;

import com.srping.aspectfilter.ex2.model.Product;
import com.srping.aspectfilter.ex2.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
//@RestController
public class ProductController {

    private ProductService productService;

    /**
     * 판매자가 자신이 판매하고 있는 컬렉션의 목록을 호출하는 엔드 포인트
     */
    @GetMapping("/find")
    public List<Product> findProducts(){
        return productService.findProducts();
    }


}
