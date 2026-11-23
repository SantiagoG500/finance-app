package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.infraestructure.driver_adapters.feign.dto.RestCountryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "restcountries-api", url = "${services.restcountries.url}")
public interface RestCountriesFeignClient {

    @GetMapping("/currency/{currency}")
    List<RestCountryResponseDTO> getCountriesByCurrency(
            @PathVariable("currency") String currency,
            @RequestParam("fields") String fields
    );

}
