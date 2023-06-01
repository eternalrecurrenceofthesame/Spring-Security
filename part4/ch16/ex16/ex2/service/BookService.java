package com.spring.securityaspect.ex2.service;

import com.spring.securityaspect.ex2.domain.Employee;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

//@Service
public class BookService {

    /**
     * 간단하게 직원을 저장하는 Map 필드
     *
     * Emma - reader (직원 세부 정보를 얻을 수 있음)
     * Natalie - researcher (직원 검색만 가능함)
     *
     * 즉 엠마의 세부 정보는 얻을 수 있지만 나탈리의 세부 정보는 얻을 수 없다.
     */
    private Map<String, Employee> records =
            Map.of("emma",
                    new Employee("Emma Thompson",
                            List.of("SpringSecurityInAction"),
                            List.of("accountant", "reader")),
                    "natalie",
                    new Employee("Natalie Parker",
                    List.of("SpringInAction6"),
                    List.of("researcher")));

    /**
     * 직원이 읽은 책의 정보를 조회하고, 사후 권한 부여를 적용해서 return 값인 returnObject(Employee) 의 역할 중
     * reader 로 지정된 직원의 세부 정보만 읽을 수 있다.
     *
     * 한마디로 엠마의 역할은 reader 이기 때문에 세부 정보를 볼 수있고, 나탈리의 세부 정보는 못 봄
     */
    @PostAuthorize("returnObject.roles.contains('reader')")
    public Employee getBookDetails(String name){
        return records.get(name); // returnObject
    }

}
