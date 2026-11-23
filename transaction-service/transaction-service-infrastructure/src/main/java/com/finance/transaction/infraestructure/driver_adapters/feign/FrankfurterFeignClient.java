package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.infraestructure.driver_adapters.feign.dto.FrankfurterResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "frankfurter-api", url = "${services.frankfurter.url}")
public interface FrankfurterFeignClient {

    @GetMapping("/latest")
    FrankfurterResponseDTO getLatestRate(
            @RequestParam("base") String base,
            @RequestParam("symbols") String symbols
    );

}
