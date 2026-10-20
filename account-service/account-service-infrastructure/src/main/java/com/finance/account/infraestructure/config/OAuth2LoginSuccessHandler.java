package com.finance.account.infraestructure.config;

import com.finance.account.domain.usecase.SincGoogleUserUseCase;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final SincGoogleUserUseCase sincGoogleUserUseCase;

//  TODO: Force redirection, now it redirects to a blank page
//        For now a workaround is made to pass a JSON instead
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String externalId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String displayName = oAuth2User.getAttribute("name");

        sincGoogleUserUseCase.iniciar(
                externalId,
                email,
                displayName
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\": \"success\", \"email\": \"" + email + "\"}");
        response.getWriter().flush();
    }


}
