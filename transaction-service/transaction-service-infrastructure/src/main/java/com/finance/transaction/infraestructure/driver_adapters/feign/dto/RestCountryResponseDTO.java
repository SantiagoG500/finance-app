package com.finance.transaction.infraestructure.driver_adapters.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestCountryResponseDTO {

    private CountryNameDTO name;
    private Map<String, CurrencyInfoDTO> currencies;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountryNameDTO {
        private String common;
        private String official;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CurrencyInfoDTO {
        private String name;
        private String symbol;
    }
}
