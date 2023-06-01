package com.spring.securityaspect.ex1.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

//@Service
public class NameService {

    private Map<String, List<String>> secretNames =
            Map.of("john", List.of("Energico", "Perfetco"),
                    "emma", List.of("Fantastico"));

    /**
     * 매서드의 매개변수를 나타내려면 #매개변수 이름으로 표현한다.
     */
    @PreAuthorize("#name == authentication.principal.username")
    public List<String> getSecretNames(String name){
        return secretNames.get(name);
    }




}
