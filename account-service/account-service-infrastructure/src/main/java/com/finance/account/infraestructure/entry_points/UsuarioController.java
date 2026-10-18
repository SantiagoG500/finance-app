package com.finance.account.infraestructure.entry_points;

import com.finance.account.domain.model.Usuario;
import com.finance.account.domain.usecase.SincGoogleUserUseCase;
import com.finance.account.domain.usecase.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// TODO: Mappings could improve
//       add DTOs and improved validation if needed

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final SincGoogleUserUseCase sincGoogleUserUseCase;

    @PostMapping("/save")
    public ResponseEntity<Usuario> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioCreado = sincGoogleUserUseCase.iniciar(
                usuario.getExternalId(),
                usuario.getEmail(),
                usuario.getDisplayName()
        );
        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorExternalId(@PathVariable("id") UUID id) {
        System.out.println(">>> [GET CONTROLLER] Received path variable ID: " + id);
        Usuario usuario = usuarioUseCase.obtenerUsuarioPorExternalId(id);

        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

//    TODO: It could ask for the id in the path
    @PutMapping("/update/")
    public ResponseEntity<Usuario> actualizarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioUseCase.actualizarUsuario(usuario);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> eliminarUsuarioPorExternalId(@PathVariable("id") UUID id) {
        usuarioUseCase.eliminarUsuarioPorExternalId(id);
        return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.OK);
    }
}
