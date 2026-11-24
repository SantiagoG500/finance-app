package com.finance.account.infraestructure.entry_points;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.UsuarioUseCase;
import com.finance.account.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateAccountStatusRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateBaseCurrencyRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateUsuarioRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase useCase;

    @GetMapping("/get/{id}")
    public ResponseEntity<UsuarioResponseDTO> getById(@PathVariable("id") UUID id, Authentication authentication) {
        Usuario usuario = verifyOwnership(id, authentication);
        return ResponseEntity.ok(UsuarioResponseDTO.fromDomain(usuario));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUsuarioRequestDTO request,
            Authentication authentication) {
        verifyOwnership(id, authentication);
        Usuario actualizado = useCase.actualizarUsuario(
                id,
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getAvatarUrl(),
                request.getBaseCurrency(),
                request.getIsActive()
        );
        return ResponseEntity.ok(UsuarioResponseDTO.fromDomain(actualizado));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MessageResponseDTO> deleteById(@PathVariable("id") UUID id, Authentication authentication) {
        verifyOwnership(id, authentication);
        useCase.eliminarUsuarioPorId(id);
        return ResponseEntity.ok(new MessageResponseDTO("Usuario eliminado con éxito"));
    }

    @PatchMapping("/{id}/currency")
    public ResponseEntity<MessageResponseDTO> updateBaseCurrency(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateBaseCurrencyRequestDTO request,
            Authentication authentication) {
        verifyOwnership(id, authentication);
        Usuario usuarioActualizado = useCase.actualizarMonedaBase(id, request.getBaseCurrency());
        return ResponseEntity.ok(new MessageResponseDTO("Divisa base se cambió a " + usuarioActualizado.getBaseCurrency()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MessageResponseDTO> changeAccountStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateAccountStatusRequestDTO request,
            Authentication authentication) {
        verifyOwnership(id, authentication);
        Usuario usuarioActualizado = useCase.cambiarEstadoCuenta(id, request.getActive());
        String message = Boolean.TRUE.equals(usuarioActualizado.getIsActive())
                ? "La cuenta " + usuarioActualizado.getEmail() + " ha sido reactivada"
                : "La cuenta " + usuarioActualizado.getEmail() + " ha sido suspendida";
        return ResponseEntity.ok(new MessageResponseDTO(message));
    }

    private Usuario verifyOwnership(UUID id, Authentication authentication) {
        Usuario usuario = useCase.obtenerUsuarioPorId(id);
        if (authentication == null
                || authentication.getName() == null
                || usuario.getEmail() == null
                || !authentication.getName().trim().equalsIgnoreCase(usuario.getEmail().trim())) {
            throw new AccessDeniedException("No tiene permisos para modificar esta cuenta");
        }
        return usuario;
    }
}
