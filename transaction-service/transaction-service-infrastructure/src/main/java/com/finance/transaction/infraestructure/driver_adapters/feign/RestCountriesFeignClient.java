package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "restcountries-api", url = "${services.restcountries.url}")
public interface RestCountriesFeignClient {

    @GetMapping("/countries/v5")
    JsonNode searchCountries(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("q") String query
    );

}
