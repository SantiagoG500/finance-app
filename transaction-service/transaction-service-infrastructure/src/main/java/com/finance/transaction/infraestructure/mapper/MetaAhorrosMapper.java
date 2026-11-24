package com.finance.transaction.infraestructure.mapper;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.entry_points.dto.CreateMetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.UpdateMetaAhorroRequestDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MetaAhorrosMapper {

    public MetaAhorro entityToMetaAhorro(MetaAhorrosEntity metaAhorrosEntity) {
        if (metaAhorrosEntity == null) {
            return null;
        }
        return new MetaAhorro(
                metaAhorrosEntity.getId() != null ? metaAhorrosEntity.getId().toString() : null,
                metaAhorrosEntity.getUserId() != null ? metaAhorrosEntity.getUserId().toString() : null,
                metaAhorrosEntity.getTitle(),
                metaAhorrosEntity.getTargetAmount(),
                metaAhorrosEntity.getCurrentAmount(),
                metaAhorrosEntity.getMonthlyContribution(),
                metaAhorrosEntity.getCurrency(),
                metaAhorrosEntity.getTargetDate(),
                metaAhorrosEntity.getCreatedAt()
        );
    }

    public MetaAhorrosEntity metaAhorroToEntity(MetaAhorro metaAhorro) {
        if (metaAhorro == null) {
            return null;
        }
        return new MetaAhorrosEntity(
                parseUuid(metaAhorro.getId()),
                parseUuid(metaAhorro.getExternalUserId()),
                metaAhorro.getTitle(),
                metaAhorro.getTargetAmount(),
                metaAhorro.getCurrentAmount(),
                metaAhorro.getMonthlyContribution(),
                metaAhorro.getTargetDate(),
                metaAhorro.getCurrency(),
                metaAhorro.getCreatedAt()
        );
    }

    public MetaAhorro toMetaAhorro(MetaAhorrosEntity metaAhorrosEntity) {
        return entityToMetaAhorro(metaAhorrosEntity);
    }

    public MetaAhorrosEntity toMetaAhorrosEntity(MetaAhorro metaAhorro) {
        return metaAhorroToEntity(metaAhorro);
    }

    public MetaAhorro requestDtoToMetaAhorro(CreateMetaAhorroRequestDTO request, String userId) {
        if (request == null) {
            return null;
        }
        return new MetaAhorro(
                null,
                userId,
                request.getTitle(),
                request.getTargetAmount(),
                request.getCurrentAmount(),
                request.getMonthlyContribution(),
                request.getCurrency() != null ? request.getCurrency().trim().toUpperCase() : null,
                request.getTargetDate(),
                null
        );
    }

    public MetaAhorro requestDtoToMetaAhorro(String id, UpdateMetaAhorroRequestDTO request, String userId) {
        if (request == null) {
            return null;
        }
        return new MetaAhorro(
                id,
                userId,
                request.getTitle(),
                request.getTargetAmount(),
                request.getCurrentAmount(),
                request.getMonthlyContribution(),
                request.getCurrency() != null ? request.getCurrency().trim().toUpperCase() : null,
                request.getTargetDate(),
                null
        );
    }

    public MetaAhorro requestDtoToMetaAhorro(MetaAhorroRequestDTO request, String userId) {
        return requestDtoToMetaAhorro(null, request, userId);
    }

    public MetaAhorro requestDtoToMetaAhorro(String id, MetaAhorroRequestDTO request, String userId) {
        if (request == null) {
            return null;
        }
        return new MetaAhorro(
                id,
                userId,
                request.getTitle(),
                request.getTargetAmount(),
                request.getCurrentAmount(),
                request.getMonthlyContribution(),
                request.getCurrency() != null ? request.getCurrency().trim().toUpperCase() : null,
                request.getTargetDate(),
                null
        );
    }

    public MetaAhorroResponseDTO metaAhorroToResponseDto(MetaAhorro metaAhorro) {
        if (metaAhorro == null) {
            return null;
        }
        return new MetaAhorroResponseDTO(
                metaAhorro.getId(),
                metaAhorro.getTitle(),
                metaAhorro.getTargetAmount(),
                metaAhorro.getCurrentAmount(),
                metaAhorro.getMonthlyContribution(),
                metaAhorro.getCurrency(),
                metaAhorro.getTargetDate(),
                metaAhorro.getCreatedAt()
        );
    }

    private UUID parseUuid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(value.trim());
    }
}
