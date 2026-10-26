package com.finance.account.infraestructure.config;

import com.finance.account.domain.gateway.NotificationGateway;
import com.finance.account.domain.model.UsuarioGateWay;
import com.finance.account.domain.usecase.SincGoogleUserUseCase;
import com.finance.account.domain.usecase.UsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioGateWay usuarioGateWay) {
        return new UsuarioUseCase(usuarioGateWay);
    }

    @Bean
    public SincGoogleUserUseCase sincGoogleUserUseCase(
            UsuarioGateWay usuarioGateWay,
            NotificationGateway notificationGateway
    ) {
        return new SincGoogleUserUseCase(usuarioGateWay, notificationGateway);
    }
}
