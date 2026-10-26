package com.finance.transaction.infraestructure.mapper;

import com.finance.transaction.domain.model.MetaAhorro;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.MetaAhorrosEntity;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroRequestDTO;
import com.finance.transaction.infraestructure.entry_points.dto.MetaAhorroResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MetaAhorrosMapper {

    // --- Entity <-> Domain Mappings ---

    public MetaAhorro entityToMetaAhorro(MetaAhorrosEntity metaAhorrosEntity) {
        if (metaAhorrosEntity == null) {
            return null;
        }
        return new MetaAhorro(
                metaAhorrosEntity.getId(),
                metaAhorrosEntity.getExternalUserId(),
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
                metaAhorro.getId(),
                metaAhorro.getExternalUserId(),
                metaAhorro.getTitle(),
                metaAhorro.getTargetAmount(),
                metaAhorro.getCurrentAmount(),
                metaAhorro.getMonthlyContribution(),
                metaAhorro.getCurrency(),
                metaAhorro.getTargetDate(),
                metaAhorro.getCreatedAt()
        );
    }

    // Backwards-compatible aliases
    public MetaAhorro toMetaAhorro(MetaAhorrosEntity metaAhorrosEntity) {
        return entityToMetaAhorro(metaAhorrosEntity);
    }

    public MetaAhorrosEntity toMetaAhorrosEntity(MetaAhorro metaAhorro) {
        return metaAhorroToEntity(metaAhorro);
    }

    // --- DTO <-> Domain Mappings ---

    public MetaAhorro requestDtoToMetaAhorro(MetaAhorroRequestDTO request, String externalUserId) {
        return requestDtoToMetaAhorro(null, request, externalUserId);
    }

    public MetaAhorro requestDtoToMetaAhorro(String id, MetaAhorroRequestDTO request, String externalUserId) {
        if (request == null) {
            return null;
        }
        return new MetaAhorro(
                id,
                externalUserId,
                request.getTitle(),
                request.getTargetAmount(),
                request.getCurrentAmount(),
                request.getMonthlyContribution(),
                request.getCurrency(),
                request.getTargetDate(),
                null // Handled by UseCase
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

}
