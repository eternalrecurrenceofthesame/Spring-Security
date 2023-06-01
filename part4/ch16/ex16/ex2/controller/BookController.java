package com.spring.securityaspect.ex2.controller;

import com.spring.securityaspect.ex2.domain.Employee;
import com.spring.securityaspect.ex2.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
//@RestController
public class BookController {

    private BookService bookService;

    /**
     * 직원의 세부 정보를 조회하는 엔드 포인트
     */
    @GetMapping("/book/details/{name}")
    public Employee getDetails(@PathVariable String name){
        return bookService.getBookDetails(name);
    }
}
