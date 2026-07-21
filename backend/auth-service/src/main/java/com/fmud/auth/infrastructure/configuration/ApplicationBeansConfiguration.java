package com.fmud.auth.infrastructure.configuration;

import com.fmud.auth.application.port.out.PasswordHasherPort;
import com.fmud.auth.application.port.out.TokenProviderPort;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.application.usecase.GetCurrentUserService;
import com.fmud.auth.application.usecase.LoginService;
import com.fmud.auth.infrastructure.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class ApplicationBeansConfiguration {
    @Bean
    LoginService loginService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        return new LoginService(users, passwordHasher, tokenProvider);
    }

    @Bean
    GetCurrentUserService getCurrentUserService(UserRepositoryPort users) {
        return new GetCurrentUserService(users);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
