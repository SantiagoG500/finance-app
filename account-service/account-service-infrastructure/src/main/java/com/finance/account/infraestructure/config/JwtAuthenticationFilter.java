package com.finance.account.infraestructure.config;

import com.finance.account.domain.gateway.TokenGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenGateway tokenGateway;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String headerAuth = request.getHeader("Authorization");
        String token = parseJwt(request);

        if (token != null) {
            if (tokenGateway.validateToken(token)) {
                String email = tokenGateway.extractEmail(token);
                log.info("JWT Filter: Authenticated user '{}' for path [{}] {}", email, request.getMethod(), request.getRequestURI());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.emptyList()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("JWT Filter: Token validation failed for path [{}] {}", request.getMethod(), request.getRequestURI());
            }
        } else if (StringUtils.hasText(headerAuth)) {
            log.warn("JWT Filter: Authorization header was present but could not be parsed into a token for path [{}] {}", request.getMethod(), request.getRequestURI());
        } else {
            log.debug("JWT Filter: No Authorization header present for path [{}] {}", request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (!StringUtils.hasText(headerAuth)) {
            return null;
        }

        String trimmed = headerAuth.trim();
        while (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            trimmed = trimmed.substring(7).trim();
        }

        return trimmed.isEmpty() ? null : trimmed;
    }
}
