package io.namjune.springboot.config.auth;

import io.namjune.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOauth2UserService customOauth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // csrf 403 error
                .csrf().disable()
                // h2-console
                .headers().frameOptions().disable()

                .and()
                // url별 권한 관리 설정 옵션의 시작점
                .authorizeRequests()
                // root와 static은 모두에게, profile 조회 API
                .antMatchers("/", "/profile", "/css/**", "/images/**", "/js/**", "/h2-console/**").permitAll()
                // api v1은 USER Role 가진 사용자에게만
                .antMatchers("/api/v1/**").hasRole(Role.USER.name())
                // 그 외는 로그인 사용자에게만
                .anyRequest().authenticated()

                .and()
                .logout()
                .logoutSuccessUrl("/")

                .and()
                .oauth2Login()
                //OAuth2 로그인 성공 이후 사용자 정보 가져올 때 설정 담당
                .userInfoEndpoint()
                //소셜 로그인 성공 시 후속 조치를 진행할 UserService 인터페이스의 구현체 등록
                //리소스 서버(소셜 서비스들)에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능을 명시
                .userService(customOauth2UserService);
    }
}
