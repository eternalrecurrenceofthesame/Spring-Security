package authorizationserver.jpa.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * OAuth2AuthorizationConsent domain 을 매핑한 엔티티
 */
//@Entity
//@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
public class AuthorizationConsent {

    //@Id
    private String registeredClientId;
    //@Id
    private String principalName;
    @Column(length = 1000)
    private String authorities;

    public String getRegisteredClientId() {
        return registeredClientId;
    }

    public void setRegisteredClientId(String registeredClientId) {
        this.registeredClientId = registeredClientId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    /**
     * JPA 에서 값 타입을 사용할 때는 Serializable 인터페이스를 구현해야 한다.
     * 값 타입을 비교하기 위해 equals and hash code 를 정의해야 한다.
     *
     * IdClass 를 사용해서 부모의 복합키를 자식의 비식별 관계로 매핑할 때도 마찬가지이다.
     *
     * 이경우 그냥 복합키를 매핑한듯 ?
     */
    public static class AuthorizationConsentId implements Serializable{

        private String registeredClientId;
        private String principalName;

        @Override
        public boolean equals(Object o) {

            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            AuthorizationConsentId that = (AuthorizationConsentId) o;
            return registeredClientId.equals(that.registeredClientId) && principalName.equals(that.principalName);

        }

        @Override
        public int hashCode() {
            return Objects.hash(registeredClientId, principalName);
        }

    }
}
