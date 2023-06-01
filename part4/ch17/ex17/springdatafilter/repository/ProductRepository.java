package com.srping.aspectfilter.springdatafilter.repository;

import com.srping.aspectfilter.springdatafilter.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.prepost.PostFilter;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * SpEL 을 사용할 때 ?#{} 이런 식으로 표현한다.
     *
     * @param text
     * @return
     */
    @Query("select p from Product p where p.name like %:text% and p.owner=?#{authentication.name}")
    List<Product> findProductByNameContains(String text);
}
