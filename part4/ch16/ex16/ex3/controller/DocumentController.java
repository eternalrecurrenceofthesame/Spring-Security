package com.spring.securityaspect.ex3.controller;

import com.spring.securityaspect.ex3.model.Document;
import com.spring.securityaspect.ex3.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class DocumentController {

    private DocumentService documentService;

    @GetMapping("/documents/{code}")
    public Document getDetails(@PathVariable String code){
        return documentService.getDocument1(code);
    }
}
