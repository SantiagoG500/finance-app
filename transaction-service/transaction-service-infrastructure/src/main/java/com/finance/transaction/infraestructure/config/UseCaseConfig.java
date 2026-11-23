package com.finance.transaction.infraestructure.config;

import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import com.finance.transaction.domain.usecase.DivisasUseCase;
import com.finance.transaction.domain.usecase.MetaAhorrosUseCase;
import com.finance.transaction.domain.usecase.ProyeccionesUseCase;
import com.finance.transaction.domain.usecase.TransaccionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProyeccionesUseCase proyeccionesUseCase(
            TransaccionGateway tGateway,
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway
    ) {
        return new ProyeccionesUseCase(tGateway, mGateway, notificationGateway);
    }

    @Bean
    public TransaccionUseCase transaccionUseCase(
            TransaccionGateway tGateway,
            NotificationGateway notificationGateway
    ) {
        return new TransaccionUseCase(tGateway, notificationGateway);
    }

    @Bean
    public MetaAhorrosUseCase metaAhorrosUseCase(
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway
    ) {
        return new MetaAhorrosUseCase(mGateway, notificationGateway);
    }

    @Bean
    public DivisasUseCase divisasUseCase(
            ProyeccionesUseCase proyeccionesUseCase,
            ExchangeRateGateway exchangeRateGateway,
            CurrencyMetadataGateway currencyMetadataGateway
    ) {
        return new DivisasUseCase(proyeccionesUseCase, exchangeRateGateway, currencyMetadataGateway);
    }
}
