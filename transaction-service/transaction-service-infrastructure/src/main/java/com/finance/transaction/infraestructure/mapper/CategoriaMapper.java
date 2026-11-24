package com.finance.transaction.infraestructure.mapper;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.infraestructure.driver_adapters.jpa_repository.entity.CategoriaEntity;
import com.finance.transaction.infraestructure.entry_points.dto.CategoriaResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.CreateCategoriaRequestDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoriaMapper {

    public Categoria entityToCategoria(CategoriaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Categoria(
                entity.getId() != null ? entity.getId().toString() : null,
                entity.getUserId() != null ? entity.getUserId().toString() : null,
                entity.getName(),
                entity.getType()
        );
    }

    public CategoriaEntity categoriaToEntity(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaEntity(
                parseUuid(categoria.getId()),
                parseUuid(categoria.getUserId()),
                categoria.getName(),
                categoria.getType()
        );
    }

    public Categoria requestDtoToCategoria(CreateCategoriaRequestDTO request, String userId) {
        if (request == null) {
            return null;
        }
        return new Categoria(
                null,
                userId,
                request.getName() != null ? request.getName().trim() : null,
                request.getType()
        );
    }

    public CategoriaResponseDTO categoriaToResponseDto(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getUserId(),
                categoria.getName(),
                categoria.getType()
        );
    }

    private UUID parseUuid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(value.trim());
    }
}
