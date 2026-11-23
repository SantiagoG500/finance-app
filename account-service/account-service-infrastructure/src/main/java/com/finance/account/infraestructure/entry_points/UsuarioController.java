package com.finance.account.infraestructure.entry_points;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.UsuarioUseCase;
import com.finance.account.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateAccountStatusRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.UpdateBaseCurrencyRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;

    @PostMapping({"/save", ""})
    public ResponseEntity<Usuario> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioCreado = usuarioUseCase.guardarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    @GetMapping({"/get/{id}", "/{id}"})
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable("id") UUID id) {
        Usuario usuario = usuarioUseCase.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping({"/email/{email}", "/by-email/{email}"})
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable("email") String email) {
        Usuario usuario = usuarioUseCase.obtenerUsuarioPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping({"/update", ""})
    public ResponseEntity<Usuario> actualizarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioUseCase.actualizarUsuario(usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping({"/delete/{id}", "/{id}"})
    public ResponseEntity<MessageResponseDTO> eliminarUsuarioPorId(@PathVariable("id") UUID id) {
        usuarioUseCase.eliminarUsuarioPorId(id);
        return ResponseEntity.ok(new MessageResponseDTO("Usuario eliminado exitosamente"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Usuario> cambiarEstadoCuenta(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateAccountStatusRequestDTO request
    ) {
        Usuario usuarioActualizado = usuarioUseCase.cambiarEstadoCuenta(id, request.getActive());
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PutMapping("/{id}/currency")
    public ResponseEntity<Usuario> actualizarMonedaBase(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateBaseCurrencyRequestDTO request
    ) {
        Usuario usuarioActualizado = usuarioUseCase.actualizarMonedaBase(id, request.getBaseCurrency());
        return ResponseEntity.ok(usuarioActualizado);
    }
}
