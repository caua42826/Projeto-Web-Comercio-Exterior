package br.com.projeto.comercio_exteriror.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String destino = "/";
        if (authentication.getPrincipal() instanceof UsuarioPrincipal principal
                && principal.isDeveAlterarSenha()) {
            destino = "/conta/senha?obrigatoria";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            destino = "/admin";
        }
        response.sendRedirect(request.getContextPath() + destino);
    }
}
