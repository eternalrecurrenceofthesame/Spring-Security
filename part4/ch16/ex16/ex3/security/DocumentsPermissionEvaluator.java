package com.spring.securityaspect.ex3.security;

import com.spring.securityaspect.ex3.model.Document;
import com.spring.securityaspect.ex3.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@AllArgsConstructor
@Component
public class DocumentsPermissionEvaluator implements PermissionEvaluator {

    /** 첫 번째 메서드 */
    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        /*

        Document document = (Document) target;

        String p = (String) permission;

        boolean admin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(p));

        /**
         * 사용자가 운영자이거나 문서의 소유자면 사용 권한을 부여한다. true
         *
        return admin || document.getOwner().equals(authentication.getName());
        */

        return false; // 사용하지 않는 경우 false 를 지정하자.
    }

    private DocumentRepository documentRepository;

    /** 두 번째 메서드 */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {

        String code = targetId.toString(); // 객체 ID 로 객체를 얻는다
        Document document = documentRepository.findDocument(code);

        String p = (String) permission;

        boolean admin = authentication.getAuthorities() // 사용자가 운영자인지 확인한다.
                .stream()
                .anyMatch(a -> a.getAuthority().equals(p));

        return admin || document.getOwner().equals(authentication.getName()); // 운영자이거나 문서 소유자이면 문서에 접근한다.
    }
}
