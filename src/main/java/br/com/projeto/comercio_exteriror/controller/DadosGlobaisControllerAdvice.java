package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.security.UsuarioPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class DadosGlobaisControllerAdvice {
    @ModelAttribute
    public void adicionarDadosDoUsuario(Model model, Authentication authentication) {
        boolean autenticado = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("usuarioAutenticado", autenticado ? authentication.getName() : null);
        model.addAttribute("isAdmin", autenticado && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("podeGerenciarDados", autenticado && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("DADOS_GERENCIAR")
                        || authority.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("deveAlterarSenha", autenticado
                && authentication.getPrincipal() instanceof UsuarioPrincipal principal
                && principal.isDeveAlterarSenha());
    }
}
