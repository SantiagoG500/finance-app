package com.finance.account.infraestructure.entry_points;

import com.finance.account.domain.model.AuthResult;
import com.finance.account.domain.usecase.AuthUseCase;
import com.finance.account.infraestructure.entry_points.dto.AuthResponseDTO;
import com.finance.account.infraestructure.entry_points.dto.ChangePasswordRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.LoginRequestDTO;
import com.finance.account.infraestructure.entry_points.dto.MessageResponseDTO;
import com.finance.account.infraestructure.entry_points.dto.RegisterRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResult result = authUseCase.register(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(result));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResult result = authUseCase.login(
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.getToken())
                .body(new MessageResponseDTO("Se ha iniciado sesión correctamente"));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        authUseCase.logout(authorizationHeader);
        return ResponseEntity.ok(new MessageResponseDTO("Sesión cerrada exitosamente"));
    }

    @PatchMapping("/password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null
                || !authentication.getName().trim().equalsIgnoreCase(request.getEmail().trim())) {
            throw new AccessDeniedException("No tiene permisos para modificar esta cuenta");
        }
        authUseCase.changePassword(
                request.getEmail(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(new MessageResponseDTO("Contraseña actualizada correctamente"));
    }

    private AuthResponseDTO toDto(AuthResult result) {
        return AuthResponseDTO.builder()
                .token(result.getToken())
                .type("Bearer")
                .userId(result.getUsuario().getId())
                .email(result.getUsuario().getEmail())
                .fullName(result.getUsuario().getFullName())
                .build();
    }
}
