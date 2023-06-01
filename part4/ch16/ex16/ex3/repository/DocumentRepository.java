package com.spring.securityaspect.ex3.repository;

import com.spring.securityaspect.ex3.model.Document;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class DocumentRepository {

    /**
     * 각 고유한 코드를 가진 문서가 저장된다고 가정한다.
     */
    private Map<String, Document> documents =
            Map.of("abc123", new Document("natalie"),
                    "qwe123", new Document("natalie"),
                    "asd555", new Document("emma"));

    /**
     * @param code
     * @return Document
     */
    public Document findDocument(String code){
        return documents.get(code);
    }
}
