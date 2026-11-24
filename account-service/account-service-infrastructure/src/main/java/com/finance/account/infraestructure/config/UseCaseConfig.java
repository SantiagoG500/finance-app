package com.finance.account.infraestructure.config;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.gateway.PasswordEncoderGateway;
import com.finance.account.domain.gateway.TokenBlacklistGateway;
import com.finance.account.domain.gateway.TokenGateway;
import com.finance.account.domain.model.UsuarioGateWay;
import com.finance.account.domain.usecase.AuthUseCase;
import com.finance.account.domain.usecase.UsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioGateWay uGateway,
                                         NotificationGateway notificationGateway,
                                         PasswordEncoderGateway passwordEncoderGateway) {
        return new UsuarioUseCase(uGateway, notificationGateway, passwordEncoderGateway);
    }

    @Bean
    public AuthUseCase authUseCase(UsuarioGateWay usuarioGateWay,
                                   PasswordEncoderGateway passwordEncoderGateway,
                                   TokenGateway tokenGateway,
                                   NotificationGateway notificationGateway,
                                   TokenBlacklistGateway tokenBlacklistGateway) {
        return new AuthUseCase(usuarioGateWay, passwordEncoderGateway, tokenGateway, notificationGateway, tokenBlacklistGateway);
    }
}
