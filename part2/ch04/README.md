# 암호 처리 

인증 공급자 -> 사용자 세부 정보 서비스 -> 암호 인코더 97p

## PasswordEcnoder 계약의 이해
 
사용자 세부 정보 서비스에서 사용자 이름으로 사용자를 찾으면 인증 공급자는 암호 인코더를 이용해서 암호를 검증한다.

사용자가 요청한 암호는 인코딩을 통해서 데이터베이스나 LDAP 서버에 저장된다.

### PasswordEncoder 계약의 정의

```
* PasswordEncoder 계약 알아보기

encode 메서드는 암호의 해시를 제공하거나 암호화를 수행하는 일을 한다.
matches 메서드는 인코딩된 문자열이 원시 암호와 일치하는지 확인하는데 사용된다.
upgradeEncoding 메서드는 기본 값이 false. true 를 반환하도록 재정의하면 인코딩된 암호를 보안 향상을 위해 다시 인코딩한다.

encode 로 반환된 암호는 항상 같은 PasswordEncoder 의 matches 로 검중할 수 있어야 한다.
```
### PasswordEncoder 의 제공된 구현 선택
```
* 스프링 시큐리티가 제공하는 구현 인코더

NoOpPasswordEncoder - 인코딩하지 않고 일반 텍스트를 그대로 사용한다. (프로덕션에서 사용 금지)
StandardPasswordEncoder - SHA-256 을 이용해 암호를 해시한다. (구식 방법 사용 금지)

Pbkdf2PasswordEncoder - PBKDF2 를 사용한다
BCryptPasswordEncoder - bcrypt 해싱 함수로 암호를 인코딩한다.
SCryptPasswordEncoder - scrypt 해싱 함수로 암호를 인코딩한다.

해싱과 알고리즘에 관한 내용 참고 livebook.manning.com/book/real-world-cryptography/chater-2
```
```
* Pbkf2PasswordEncoder

PasswordEncoder p = new Pbkdf2PasswordEncoder();
PasswordEncoder p = new Pbkdf2PasswordEncoder("secret");
PasswordEncoder p = new Pbkdf2PasswordEncoder("secret", 185000, 256); // 디폴트 값 185000, 256 

Pbkf2 는 반복 횟수 인수만큼 HMAC 를 수행하는 느리고 단순한 해싱 함수
HMAC 에 관한 내용 참고 livebook.manning.com/book/real-world-cryptography/chatper-3 

인코딩 프로세스에 이용되는 키의 값, 암호 인코딩 반복 횟수, 해시의 크기 
이러한 값들은 성능에 영향을 미치기 때문에 신중하게 절충해야 한다.
```
```
* BCryptPasswordEncoder

PasswordEncoder p = new BCryptPasswordEncoder();
PasswordEncoder p = new BCryptPasswordEncoder(4);
SecureRandom s = SecureRandom.getInstanceStrong();
PasswordEncoder p = new BCryptPasswordEncoder(4, s);

로그 라운드는 해싱 작업이 이용하는 반복 횟수에 영향을 준다 4 ~ 31 사이여야 한다.
인코딩에 이용되는 SecureRandom 인스턴스를 변경할 수도 있다. 102p
```
```
* SCryptPasswordEncoder

PasswordEncoder p = new SCryptPasswordEncoder();
PasswordEncoder p = new SCryptPasswordEncoder(16384, 8, 1, 32, 64);

순서대로 CPU 비용, 메모리 비용, 병렬화 계수, 키 길이, 솔트 길이를 지정할 수 있다.
```

## DelegatingPasswordEncoder 를 이용한 여러 인코딩 전략 

DelegatingPasswordEncoder 는 특정 애플리케이션 버전부터 인코딩 알고리즘이 변경될 경우에 사용할 수 있다.

현재 사용되는 알고리즘에 취약성이 발견되어 신규 등록자의 자격 증명을 변경하고 싶지만,

기존 자격 증명을 변경하기 쉽지 않을 때 DelegatingPasswordEncoder 를 사용하면 된다. 103p

```
Delegating 인코더는 각 인코더 구현체의 인스턴스에 접두사를 이용해서 맵으로 저장하고 
접두사에 따라 필요한 인코더 구현체를 호출해서 사용하는 일종의 파사드 역할을 수행할 수 있다.

ex) 접두사 {bcrypt} 가 있는 암호를 지정하고 matches() 메서드를 호출하면 BCryptPasswordEncoder 에 위임된다.

ex04 ProjectConfig 참고 
```

### DelegatingPasswordEncoder 의 인스턴스 구현하기
```
ex04 ProjectConfig 참고 

스프링 시큐리티는 편의를 제공하기 위해 모든 PasswordEncoder 의 구현에 대한 맵을 가진 DelegatingPasswordEncoder 를
생성하는 방법을 제공한다. 간단하게 정적 메서드를 호출해서 만들 수 있음.

PasswordEncoderFactories.createDelegatingPasswordEncoder(); 
```
```
* 인코딩(Encoding)

인코딩은 주어진 입력에 대한 모든 변환을 의미한다. 문자열을 뒤집는 인코딩 함수 x 적용시  x -> ABCD -> DCBA
```
```
* 암호화(Encryption)

암호화는 출력을 얻기 위해 입력 값과 키를  지정하는 특정한 유형의 '인코딩' 이다.

(입력, 키) -> 출력 // 기본적인 암호화 인코딩
(출력, 키) -> 입력 // 출력에서 입력을 얻는 것을 역함수 복호화라고 한다. 
 
 암호화에 쓰이는 키와 복호화에 쓰는 키가 같다면 일반적으로 대칭키라고 말한다. 
 
 
 (입력, 키1) -> 출력 // 암호화
 (출력, 키2) -> 입력 // 복호화
 
암호화, 복호화에서 다른 키를 사용하면 비대칭키로 암호화가 수행된다고 말한다. (키1, 키2) 를 묶어 하나의 키 쌍이라고 한다. 
암호화에 사용되는 키1 을 '공개키', 복호화에 사용되는 키2 를 '개인키'라고 한다. '개인키'의 소유자는 복호화를 할 수 있다! 
```
```
* 해싱(Hashing)

해싱은 함수가 한 방향으로만 작동하는 특정한 유형의 인코딩이다. 출력 y 가 입력 x 에 해당하는지 확인할 수 있는 방법으로 해싱이 사용된다. 

x -> y  // 해싱 함수, y 에서 x 를 얻을 수는 없다. 
(x,y) -> boolean // 일치 함수 107p

해싱의 입력에 임의의 값을 추가할 수 있고 이 값을 솔트라고 한다. 솔트는 함수를 더 강하게 만들어 결과에서 입력을 얻는
역함수의 적용 난도를 높인다.

(x, k) -> y 
```

## 스프링 시큐리티 암호화 모듈에 관한 추가 정보
```
스프링 시큐리티는 자바 언어에서 기본으로 제공되지 않는 암호화 모듈(SSCM) 을 제공한다. 

키 생성기 - 해싱 및 암호화 알고리즘을 위한 키를 생성하는 객체
암호기 - 데이터를 암호화 및 복호화 하는 객체                 // SSCM 의 두 가지 필수기능

앞서 살펴본 인코더도 SSCM 의 일부분이다. 
```
### 키 생성하기
```
BytesKeyGenerator 및 StringKeyGenerator 는 키 성생기의 두 가지주요 유형을 나타내는 인터페이스
팩터리 클래스 KeyGenerators 로 직접 키 생성기를 만들 수 있따.
```
```
* StringKeyGenerator

StringKeyGenerator 은 스트링 키를 반환하는 메서드를 가지며 해싱, 암호화 알고리즘의 솔트 값으로 이용된다.

StringKeyGenerator stringKeyGenerator = KeyGenerators.string(); // 팩토리에서 키(솔트) 생성기 생성
String salt = stringKeyGenerator.generateKey(); // 생성기에서 솔트값을 만듦 

String salt = KeyGenerators.string().generateKey(); // 축약 버전 
```
```
* BytesKeyGenerator

BytesKeyGenerator 는 바이트 키를 반환하는 메서드와 바이트 생성된 키의 길이(바이트 수) 를 반환하는 메서드를 가진다.

BytesKeyGenerator bytesKeyGenerator = KeyGenerators.secureRandom(16); // 길이 설정
byte[] key1 = bytesKeyGenerator.generateKey(); // 생성할 때 마다 새로운 키를 생성한다.
int keyLength = bytesKeyGenerator.getKeyLength();

BytesKeyGenerator sharedKeyGenerator = KeyGenerators.shared(16); // 같은 키 값 생성 메서드
byte[] sharedKey1 = sharedKeyGenerator.generateKey();
byte[] sharedKey2 = sharedKeyGenerator.generateKey(); // 키 값이 같다.
```

### 암호화 복호화 작업에 암호기 이용하기 

SSCM 은 두 가지 암호기를 제공한다 BytesEncryptor, TextEncryptor
```
* BytesEncryptor

String salt = KeyGenerators.string().generateKey(); // 키(솔트)
String password = "secret"; // 인코딩할 패스워드
String valueToEncrypt = "HELLO"; // 패스워드를 인코딩한 값


BytesEncryptor e = Encryptors.standard(password, salt); // 표준 암호기 

표준 바이트 암호기는 256 바이트 AES 작업 모드로 CBC(암호 블록체인) 을 이용한다.
강력한 바이트 암호기는 256 바이트 AES 작업 모드로 GMC(갈루아/카운터 모드) 를 이용한다.

강력한 바이트 암호기를 이용하려면 Encryptors.stronger() 메서드 호출 

byte[] encrypt = e.encrypt(valueToEncrypt.getBytes()); // 패스워드를 인코딩
byte[] decrypt = e.decrypt(encrypt); // 인코딩된 값을 디코딩

(입력 패스워드, 솔트) -> 출력 encrypt 
```
```
* TextEncryptor

deprecated 되어서 사용되지 않는다 111p
```
