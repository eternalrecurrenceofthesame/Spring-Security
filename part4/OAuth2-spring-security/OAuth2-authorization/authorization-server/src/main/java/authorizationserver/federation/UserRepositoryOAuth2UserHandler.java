/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package authorizationserver.federation;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Example {@link Consumer} to perform JIT provisioning of an {@link OAuth2User}.
 *
 * @author Steve Riesenberg
 * @since 1.1
 */

/**
 * 이 핸들러는 데이터베이스에 저장된 UserRepository 에서 값이 저장되어 있는지 조회한 후
 * 중복 값이 아니라면 즉 값이 없으면 'OAuth2Login' 사용자 데이터를 저장한다.
 *
 * OAuth2Login 유저 인증 완료 후 데이터를 저장할 때 이런 핸들러를 구현해서 논리를 구현할 수 있다.
 *
 * OAuth2User 는 AuthenticatedPrincipal 이 된다. 이메일로 수정 예정
 */
public class UserRepositoryOAuth2UserHandler implements Consumer<OAuth2User> {

    private final UserRepository userRepository = new UserRepository();

    @Override
    public void accept(OAuth2User user) {
        if(this.userRepository.findByName(user.getName()) == null){
            System.out.println("Saving first-time user: name=" + user.getName() +
                    ", claims=" + user.getAttributes() +
                    ", authorities=" + user.getAuthorities());

            this.userRepository.save(user);
        }
    }

    static class UserRepository{

        private final Map<String, OAuth2User> userCache = new ConcurrentHashMap<>();

        public OAuth2User findByName(String name){
            return this.userCache.get(name);
        }

        public void save(OAuth2User oAuth2User){
            this.userCache.put(oAuth2User.getName(), oAuth2User);
        }
    }
}
