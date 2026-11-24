package com.finance.transaction.infraestructure.config;

import com.finance.transaction.domain.model.gateway.AccountVerificationGateway;
import com.finance.transaction.domain.model.gateway.CategoriaGateway;
import com.finance.transaction.domain.model.gateway.CurrencyMetadataGateway;
import com.finance.transaction.domain.model.gateway.ExchangeRateGateway;
import com.finance.transaction.domain.model.gateway.MetaAhorrosGateway;
import com.finance.transaction.domain.model.gateway.NotificationGateway;
import com.finance.transaction.domain.model.gateway.TransaccionGateway;
import com.finance.transaction.domain.usecase.CategoriaUseCase;
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
            NotificationGateway notificationGateway,
            ExchangeRateGateway exchangeRateGateway,
            AccountVerificationGateway accountVerificationGateway,
            CategoriaGateway categoriaGateway
    ) {
        return new ProyeccionesUseCase(
                tGateway,
                mGateway,
                notificationGateway,
                exchangeRateGateway,
                accountVerificationGateway,
                categoriaGateway
        );
    }

    @Bean
    public TransaccionUseCase transaccionUseCase(
            TransaccionGateway tGateway,
            NotificationGateway notificationGateway,
            AccountVerificationGateway accountVerificationGateway,
            CategoriaGateway categoriaGateway
    ) {
        return new TransaccionUseCase(
                tGateway,
                notificationGateway,
                accountVerificationGateway,
                categoriaGateway
        );
    }

    @Bean
    public CategoriaUseCase categoriaUseCase(
            CategoriaGateway categoriaGateway,
            TransaccionGateway transaccionGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        return new CategoriaUseCase(categoriaGateway, transaccionGateway, accountVerificationGateway);
    }

    @Bean
    public MetaAhorrosUseCase metaAhorrosUseCase(
            MetaAhorrosGateway mGateway,
            NotificationGateway notificationGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        return new MetaAhorrosUseCase(mGateway, notificationGateway, accountVerificationGateway);
    }

    @Bean
    public DivisasUseCase divisasUseCase(
            TransaccionGateway transaccionGateway,
            ExchangeRateGateway exchangeRateGateway,
            CurrencyMetadataGateway currencyMetadataGateway,
            AccountVerificationGateway accountVerificationGateway
    ) {
        return new DivisasUseCase(transaccionGateway, exchangeRateGateway, currencyMetadataGateway, accountVerificationGateway);
    }
}
