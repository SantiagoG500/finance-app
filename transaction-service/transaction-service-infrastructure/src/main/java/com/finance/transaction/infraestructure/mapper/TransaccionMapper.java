package com.finance.transaction.infraestructure.mapper;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class TransaccionMapper {

    public Transaccion entityToTransaccion(TransaccionEntity transaccionEntity) {
        if (transaccionEntity == null) {
            return null;
        }
        return new Transaccion(
                transaccionEntity.getId(),
                transaccionEntity.getExternalUserId(),
                transaccionEntity.getCategoryId(),
                transaccionEntity.getAmount(),
                transaccionEntity.getCurrency(),
                transaccionEntity.getType(),
                transaccionEntity.getFrequency(),
                transaccionEntity.getDescription(),
                transaccionEntity.getTransactionDate(),
                transaccionEntity.getCreatedAt()
        );
    }

    public TransaccionEntity transaccionToEntity(Transaccion transaccion) {
        if (transaccion == null) {
            return null;
        }
        return new TransaccionEntity(
                transaccion.getId(),
                transaccion.getExternalUserId(),
                transaccion.getCategoryId(),
                transaccion.getAmount(),
                transaccion.getCurrency(),
                transaccion.getType(),
                transaccion.getFrequency(),
                transaccion.getDescription(),
                transaccion.getTransactionDate(),
                transaccion.getCreatedAt()
        );
    }

    public Transaccion requestDtoToTransaccion(TransaccionRequestDTO request, String externalUserId) {
        if (request == null) {
            return null;
        }
        return new Transaccion(
                request.getId(),
                externalUserId,
                request.getCategoryId(),
                request.getAmount(),
                request.getCurrency(),
                request.getType(),
                request.getFrequency(),
                request.getDescription(),
                request.getTransactionDate(),
                null // Manejado por UseCase
        );
    }

    public TransaccionResponseDTO transaccionToResponseDto(Transaccion transaccion) {
        if (transaccion == null) {
            return null;
        }
        return new TransaccionResponseDTO(
                transaccion.getId(),
                transaccion.getCategoryId(),
                transaccion.getAmount(),
                transaccion.getCurrency(),
                transaccion.getType(),
                transaccion.getFrequency(),
                transaccion.getDescription(),
                transaccion.getTransactionDate(),
                transaccion.getCreatedAt()
        );
    }

}
