package com.ikernell.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .findFirst()
                .orElse("");

        String redirectUrl = "/defaultSuccessUrl";

        switch (role) {
            case "ROLE_LIDER":
                redirectUrl = "/lider/index";
                break;
            case "ROLE_COORDINADOR":
                redirectUrl = "/coordinador/index";
                break;
            case "ROLE_DESARROLLADOR":
                redirectUrl = "/desarrollador/index";
                break;
        }

        response.sendRedirect(redirectUrl);
    }
}
