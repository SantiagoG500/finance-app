package com.finance.account.infraestructure.entry_points;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.UsuarioUseCase;
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

    @PostMapping("/save")
    public ResponseEntity<Usuario> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioCreado = usuarioUseCase.guardarUsuario(usuario);
        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable("id") UUID id) {
        Usuario usuario = usuarioUseCase.obtenerUsuarioPorId(id);
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable("email") String email) {
        Usuario usuario = usuarioUseCase.obtenerUsuarioPorEmail(email);
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Usuario> actualizarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioUseCase.actualizarUsuario(usuario);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> eliminarUsuarioPorId(@PathVariable("id") UUID id) {
        usuarioUseCase.eliminarUsuarioPorId(id);
        return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.OK);
    }
}
