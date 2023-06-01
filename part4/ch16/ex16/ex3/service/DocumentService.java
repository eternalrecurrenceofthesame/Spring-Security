package com.spring.securityaspect.ex3.service;

import com.spring.securityaspect.ex3.model.Document;
import com.spring.securityaspect.ex3.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

/**
 * 서비스는 비즈니스 로직을 구현하지 않는다.
 *
 * 서비스는 호출 순서를 정의한다. 권한 부여 규칙은 서비스 계층에 적용한다.
 */
@AllArgsConstructor
@Service
public class DocumentService {

    private DocumentRepository documentRepository;

    /**
     * 첫 번째 메서드
     *
     * 역할이 어드민인 경우에만 리턴 값이 permission 된다.
     * hasPermission 메서드에 제공하는 값은 리턴 객체와 권한 String 이다.
     *
     * hasPermission 사용 권한 논리를 구현하려면 PermissionEvaluator 게약을 구현하는 객체를
     * 만들어야 한다.
     */
    @PostAuthorize("hasPermission(returnObject, 'ROLE_admin')")
    public Document getDocument1(String code){
        return documentRepository.findDocument(code);
    }

    /**
     * 두 번째 메서드
     *
     * 파라미터 값을 가져올 때 # 을 사용하는듯? id 값, 타깃 타입, 역할을 넘겨준다.
     */
    @PostAuthorize("hasPermission(#code, 'document', 'ROLE_admin')")
    public Document getDocument2(String code){
        return documentRepository.findDocument(code);
    }
}
