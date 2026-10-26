package com.finance.transaction.infraestructure.config;

import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import com.finance.transaction.domain.usecase.MetaAhorrosUseCase;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProyeccionesUseCase proyeccionesUseCase(TransaccionGateway tGateway, MetaAhorrosGateway mGateway) {
        return new ProyeccionesUseCase(tGateway, mGateway);
    }

    @Bean
    public TransaccionUseCase transaccionUseCase(TransaccionGateway tGateway) {
        return new TransaccionUseCase(tGateway);
    }

    @Bean
    public MetaAhorrosUseCase metaAhorrosUseCase(MetaAhorrosGateway mGateway) {
        return new MetaAhorrosUseCase(mGateway);
    }
}
