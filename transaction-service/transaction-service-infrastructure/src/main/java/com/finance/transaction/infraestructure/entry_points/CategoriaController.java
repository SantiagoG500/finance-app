package com.finance.transaction.infraestructure.entry_points;

import com.finance.transaction.domain.model.Categoria;
import com.finance.transaction.domain.usecase.CategoriaUseCase;
import com.finance.transaction.infraestructure.entry_points.dto.CategoriaResponseDTO;
import com.finance.transaction.infraestructure.entry_points.dto.CreateCategoriaRequestDTO;
import com.finance.transaction.infraestructure.mapper.CategoriaMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/finances/categories")
@RequiredArgsConstructor
@Validated
public class CategoriaController {

    private final CategoriaUseCase categoriaUseCase;
    private final CategoriaMapper categoriaMapper;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(
            @Valid @RequestBody CreateCategoriaRequestDTO request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        Categoria categoria = categoriaMapper.requestDtoToCategoria(request, userId);
        Categoria creada = categoriaUseCase.crearCategoria(categoria, bearerToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaMapper.categoriaToResponseDto(creada));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategoriasPorUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        List<Categoria> categorias = categoriaUseCase.listarCategoriasPorUsuario(userId, bearerToken);
        List<CategoriaResponseDTO> responseList = categorias.stream()
                .map(categoriaMapper::categoriaToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(
            @PathVariable("id") @NotBlank(message = "ID no debe estar vacío") String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String userId = JwtContextHelper.extractUserId(jwt);
        String bearerToken = JwtContextHelper.resolveBearerToken(authorizationHeader, jwt);
        categoriaUseCase.eliminarCategoria(id, userId, bearerToken);

        return ResponseEntity.noContent().build();
    }
}
