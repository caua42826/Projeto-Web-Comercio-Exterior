package br.com.projeto.comercio_exteriror.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (autenticado(authentication)) {
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin";
            }
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "erro/403";
    }

    private boolean autenticado(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
