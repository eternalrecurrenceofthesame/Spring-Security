package com.srping.aspectfilter.ex1.controller;

import com.srping.aspectfilter.ex1.model.Product;
import com.srping.aspectfilter.ex1.service.ProductService;
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
     * 판매자가 상품을 판매하고 내역을 호출하는 엔드포인트로 가정
     * 예제에 집중하기 위해 Post 를 사용하지 않고 CSRF 를 우회한다.
     *
     * @return 상품, 판매자
     */
    @GetMapping("/sell")
    public List<Product> sellProduct(){
        List<Product> products = new ArrayList<>();

        products.add(new Product("beer", "nikolai"));
        products.add(new Product("candy", "nikolai"));
        products.add(new Product("chocolate", "julien"));

        return productService.sellProducts(products);
    }

}
