# 전역 메서드 보안: 사전 사후 필터링

16 장에서는 전역 메서드 보안으로 권한 부여 규칙을 적용하는 방법을 배우고 실습했다면 이번 단원에서는 메서드 호출 전,후로 

사전, 사후 필터링 하는 방법을 배운다!

사전 필터링을 이용하면 메서드가 구현하는 비즈니스 논리는 필터링을 적용하면서, 권한 부여 규칙을 따로 분리할 수 있다! 

필터링은 권한 부여와 마찬가지로 애스펙트를 이용해서 구현한다.  469 p

```
사전 필터링: 프레임워크가 메서드를 호출하기 전에 매개 변수의 값을 필터링한다.
사후 필터링: 프레임워크가 메서드를 호출한 후 반환된 값을 필터링한다. 

참고로 사전 권한 부여 규칙을 따르지 않는 경우에는 메서드 자체가 호출되지 않고 예외를 받지만, 사전 필터링의 경우 여러 값을
파라미터로 받는 경우 애스펙트가 메서드를 호출하지만 주어진 규칙을 따르는 값만 전달한다. 

결론적으로 필터링을 적용하면 권한 부여 규칙을 준수하지 않아도 메서드를 호출하고 예외를 던지지 않으며 지정한 조건을 준수하지
않는 요소는 필터링으로 걸러낸다. 468 p

또한 사전, 사후 필터링은 컬렉션과 배열에만 적용할 수 있다. 
```
## 메서드 권한 부여를 위한 사전 필터링 적용하기 

### 사전 필터링 시나리오 구현하기

사전 필터링은 요청 컬렉션 매개변수 값을 사전에 필터링하고 로직을 수행한다.
```
* ch 17 ex 1 참고 

필터링을 구현하는 경우에도 메서드 권한 부여와 마찬가지로 구성 클래스에 @EnableGlobalMethodSecurity(prePostEnabled = true)
애노테이션을 추가해야 한다. 

@PreFilter("filterObject.owner == authentication.name") 
public List<Product> sellProducts(List<Product> products){

요청 파라미터로 넘어온 판매자의 이름과 보안 컨텍스트에 인증된 사용자가 있는지 필터링 하고 상품 판매 로직을 수행하기 위한
필터링 SpEL 식 
(쉽게 말해서 로그인한 사용자만 상품을 판매할 수 있게 필터링 하는 것임) 

@PreFilter, @PostFilter 어노테이션은 인증된 사용자(Authentication) 의 메타 데이터를 보안 컨텍스트에서 참조할 수 있다.

curl -u nikolai:12345 http://localhost:8080/sell 사용자 인증 후 sell 을 호출하면 니콜라이의 판매 목록이 보인다. 
curl -u julien:12345 http://localhost:8080/sell 사용자 인증 후 sell 을 호출하면 줄리엔의 판매 목록이 보인다. 
```
```
* 필터링시 주의 사항

필터링을 적용하려면 앞서 설명했다시피 컬렉션만 가능하다. 이때 반환되는 컬렉션을 필터링 애스펙트가 필터링을 적용하면서
인스턴스에서 기준에 맞지 않는 요소를 제거하기 때문에 변경 불가능한 컬렉션을 제공하면 안 된다.

List<Product> products = new ArrayList<>();

products.add(new Product("beer", "nikolai"));
products.add(new Product("candy", "nikolai"));
products.add(new Product("chocolate", "julien"));

ArrayList 로 변경 가능한 컬렉션을 제공하는 것은 문제되지 않지만

List.of(new Product("candy","nikolai")
        new Product("chocolate", "julien"));

List.of 를 사용해서 변경 불가능한 컬렉션을 제공하면 예외가 발생한다. 475 p
```

## 메서드 권한 부여를 위한 사후 필터링 적용하기 

### 사후 필터링 시나리오 구현하기 

사후 필터링은 반환되는 값이 정의된 규칙을 준수하는지 확인하고 규칙을 준수하지 않는 컬렉션 요소를 필터링 한다. 
```
* ch 17 ex 2 service, config 참고

사후 권한 부여의 경우 반환될 값이 주어진 권한 부여 규칙을 준수하지 않으면 프레임 워크가 아예 값을 반환하지 않으며
예외를 던지지만

사후 필터링에서는 호출자가 반환된 컬렉션을 받을 수는 있지만 필터링 규칙을 준수하는 값만 컬렉션에 들어있다.
```

## 스프링 데이터 리포지토리에 필터링 이용하기

스프링 데이터에 필터링을 적용하는 방법에는 두 가지가 있고, 적용 방식을 결정할 때는 주의해야 할 사항이 있다.

```
@PreFilter 및 @PostFitler 애노테이션으로 필터링하기
쿼리 내에서 직접 필터링 사용하기

애노테이션으로 필터링을 할때는 주의해야 한다. @PreFilter 를 사용해서 검색 조건을 사전에 필터링 하는 것은 인증 컨텍스트에서
조건을 지정하고 스프링 데이터를 통해서 검색 조건으로 필요한 데이터만 가져오기 때문에 문제되지 않지만

사후 필터링을 적용하려면 필터링 범위에 포함되는 데이터를 전부 가져온 다음에 필터링으로 하나하나 걸러내야한다. 이것은 잘못된 
디자인으로 데이터의 양이 많으면 OutofMemorError 가 발생할 수 있다.  481 p 
```

### 스프링 데이터 필터링 구현하기 
```
* 사후 필터링 + JPQL 로 필터링 특정하기 ex 17 ProductRepository, config 참고 

앞서 설명했다시피 @PostFilter 를 적용하면 데이터 베이스에서 필요하지 않은 데이터를 모두 가져온 후 필터링 한다.

@PostFilter("filterObject.owner == authentication.name")
List<Product> findProductByNameContains(String text); // 잘못된 예시

데이터를 가져온 후 필터링하지 않고 처음부터 필요한 데이터만 선택하려면 JPQL 을 작성하면서 인증 컨텍스트에서
필요한 데이터만 선택해서 가지고 오게 하면 된다.

<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring-security-data</artifactId>
</dependency> 

의존성을 추가하고 시큐리티 구성 클래스에 SecurityEvaluationContextExtension 을 빈으로 추가하면 JPQL 쿼리에 
직접 SpEL 식을 지정할 수 있게 된다. ProjectConfig 참고 

@Query("select p from Product p where p.name like %:text% and p.owner=?#{authentication.name}")

JPQL 에 SpEL 식은 ?#{} 으로 표현한다.
```

