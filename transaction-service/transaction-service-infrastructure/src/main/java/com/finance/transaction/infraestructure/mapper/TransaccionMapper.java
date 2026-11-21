package com.finance.transaction.infraestructure.mapper;

import com.finance.transaction.domain.model.Transaccion;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.TransaccionEntity;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.TransaccionResponseDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransaccionMapper {

    public Transaccion entityToTransaccion(TransaccionEntity transaccionEntity) {
        if (transaccionEntity == null) {
            return null;
        }
        return new Transaccion(
                transaccionEntity.getId() != null ? transaccionEntity.getId().toString() : null,
                transaccionEntity.getUserId() != null ? transaccionEntity.getUserId().toString() : null,
                transaccionEntity.getCategoryId() != null ? transaccionEntity.getCategoryId().toString() : null,
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
                parseUuid(transaccion.getId()),
                parseUuid(transaccion.getExternalUserId()),
                parseUuid(transaccion.getCategoryId()),
                transaccion.getAmount(),
                transaccion.getCurrency(),
                transaccion.getType(),
                transaccion.getFrequency(),
                transaccion.getDescription(),
                transaccion.getTransactionDate(),
                transaccion.getCreatedAt()
        );
    }

    public Transaccion requestDtoToTransaccion(TransaccionRequestDTO request, String userId) {
        if (request == null) {
            return null;
        }
        return new Transaccion(
                request.getId(),
                userId,
                request.getCategoryId(),
                request.getAmount(),
                request.getCurrency(),
                request.getType(),
                request.getFrequency(),
                request.getDescription(),
                request.getTransactionDate(),
                null // Manejado por UseCase o @CreatedDate
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

    private UUID parseUuid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(value.trim());
    }
}