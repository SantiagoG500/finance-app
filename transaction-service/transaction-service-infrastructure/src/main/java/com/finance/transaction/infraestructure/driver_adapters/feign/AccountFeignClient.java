package com.finance.transaction.infraestructure.driver_adapters.feign;

import com.finance.transaction.infraestructure.driver_adapters.feign.dto.AccountUserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "account-service", url = "${services.account.url}")
public interface AccountFeignClient {

    @GetMapping("/api/usuarios/get/{id}")
    AccountUserResponseDTO getUserById(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String authorizationHeader
    );

}
