package io.namjune.springboot.config.auth;

import io.namjune.springboot.config.auth.dto.OAuthAttributes;
import io.namjune.springboot.config.auth.dto.SessionUser;
import io.namjune.springboot.domain.user.User;
import io.namjune.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 로그인 서비스 구분 코드(google or naver ...)
        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();
        // 로그인 진행시 key가 되는 필드 값(like PK). google의 기본 코드는 "sub". 네이버, 카카오 등은 지원하지 않음
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        //OAuth2User에서 반환하는 사용자 정보가 Map이기 때문에 변환 작업이 필요
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        //User 생성 or 업데이트
        User user = saveOrUpdate(attributes);

        /*
          세션에 User를 그대로 저장하지 않는 이유

          SessionUser에는 인증괸 사용자 정보만을 필요로 한다.
          User를 세션에 저장하려면 직렬화를 구현해야 한다.
          하지만 순수 도메인 객체에 직렬화 코드를 넣는 것에 대해서는 생각해 볼 것이 많다.
          엔티티 클래스는 언제 다른 엔티티들과 관계를 형성할지 모른다.
          예를 들어, @OneToMany. @ManyToMany 등 자식 엔티티를 가지고 있다면 직렬화 대상에 자식 엔티티들까지 포함된다.
          이는 성능 이슈와 사이드 이펙트를 발생시킬 확률을 높인다.
          위험 부담을 가져가는 것 보다, 직렬화 기능을 가지고있는 DTO를 만들어서 활용하자.
         */
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                                     attributes.getAttributes(),
                                     attributes.getNameAttributeKey());
    }

    /**
     * 구글 사용자 정보 업데이트 되었을 때를 대비하여 update 기능 포함.
     *
     * @param attributes 인증정보
     * @return User
     */
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                // 엔티티 생성 시점은 처음 가입할 때만.
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}
