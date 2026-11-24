package com.finance.account.infraestructure.entry_points.dto;

import com.finance.account.domain.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {

    private UUID id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String baseCurrency;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static UsuarioResponseDTO fromDomain(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getFullName(),
                usuario.getAvatarUrl(),
                usuario.getBaseCurrency(),
                usuario.getIsActive(),
                usuario.getCreatedAt()
        );
    }
}
