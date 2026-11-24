package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.finance.transaction.infraestructure.driver_adapters.feign.dto.ExchangeRateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "exchangerate-api", url = "${services.exchangerate.url}")
public interface ExchangeRateFeignClient {

    @GetMapping("/{apiKey}/latest/{base}")
    ExchangeRateResponseDTO getLatestRates(
            @PathVariable("apiKey") String apiKey,
            @PathVariable("base") String base
    );

    @GetMapping("/{apiKey}/codes")
    JsonNode getSupportedCodes(@PathVariable("apiKey") String apiKey);

}
