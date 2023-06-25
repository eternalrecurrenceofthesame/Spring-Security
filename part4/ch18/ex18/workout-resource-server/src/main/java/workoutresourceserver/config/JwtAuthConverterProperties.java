package workoutresourceserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Jwt 컨버터 yml 구성 설정을 매핑하는 클래스.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "jwt.auth.converter") // 이녀석으로 yml 값을 가져올 수 있다.
public class JwtAuthConverterProperties {

    private String resourceId;
    private String principalAttribute;
}
