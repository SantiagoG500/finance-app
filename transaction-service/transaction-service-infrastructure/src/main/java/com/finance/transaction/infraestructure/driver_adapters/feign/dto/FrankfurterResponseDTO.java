package com.finance.transaction.infraestructure.driver_adapters.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrankfurterResponseDTO {

    private BigDecimal amount;
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;

}
