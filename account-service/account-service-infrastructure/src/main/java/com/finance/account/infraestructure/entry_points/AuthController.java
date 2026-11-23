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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(result.getToken())
                .type("Bearer")
                .userId(result.getUsuario().getId())
                .email(result.getUsuario().getEmail())
                .fullName(result.getUsuario().getFullName())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResult result = authUseCase.login(
                request.getEmail(),
                request.getPassword()
        );

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(result.getToken())
                .type("Bearer")
                .userId(result.getUsuario().getId())
                .email(result.getUsuario().getEmail())
                .fullName(result.getUsuario().getFullName())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponseDTO> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        authUseCase.changePassword(
                request.getEmail(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(new MessageResponseDTO("Contraseña actualizada correctamente"));
    }
}
