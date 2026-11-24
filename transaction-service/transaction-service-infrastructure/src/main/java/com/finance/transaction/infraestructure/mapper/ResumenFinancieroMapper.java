package com.finance.transaction.infraestructure.mapper;

import com.finance.transaction.domain.model.ResumenFinanciero;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.ResumenFinancieroDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ResumenFinancieroMapper {

    private final MetaAhorrosMapper metaAhorrosMapper;

    public ResumenFinancieroDTO resumenFinancieroToDto(ResumenFinanciero resumen) {
        if (resumen == null) {
            return null;
        }

        List<MetaAhorroResponseDTO> metasDto = (resumen.getMetasAhorro() != null)
                ? resumen.getMetasAhorro().stream()
                .map(metaAhorrosMapper::metaAhorroToResponseDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        Map<String, BigDecimal> porCategoriaDto = (resumen.getPorCategoria() != null)
                ? resumen.getPorCategoria()
                : Collections.emptyMap();

        String monedaBase = resumen.getMonedaBase() != null ? resumen.getMonedaBase() : resumen.getMoneda();

        return new ResumenFinancieroDTO(
                resumen.getUsuarioId(),
                resumen.getMoneda(),
                monedaBase,
                resumen.getIngresosMensuales(),
                resumen.getGastosMensuales(),
                resumen.getBalanceNetoMensual(),
                resumen.getCompromisoAhorroMensual(),
                resumen.getMargenLibre(),
                metasDto,
                resumen.getProyeccion1Ano(),
                porCategoriaDto
        );
    }

}
