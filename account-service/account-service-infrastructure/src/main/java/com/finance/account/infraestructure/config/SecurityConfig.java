package com.finance.account.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// TODO: Remove API entrypoint when testing is finished

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login**", "/error").permitAll()
//                        .requestMatchers("/api/usuarios/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2LoginSuccessHandler));

        return http.build();
    }

}
