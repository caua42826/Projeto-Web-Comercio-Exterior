package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.dto.AlterarSenhaForm;
import br.com.projeto.comercio_exteriror.service.RegraSegurancaException;
import br.com.projeto.comercio_exteriror.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/conta/senha")
public class ContaController {
    private final UsuarioService usuarioService;

    public ContaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String formulario(@RequestParam(name = "obrigatoria", required = false) String obrigatoria,
                             Model model) {
        model.addAttribute("alterarSenha", new AlterarSenhaForm());
        model.addAttribute("alteracaoObrigatoria", obrigatoria != null);
        return "conta/senha";
    }

    @PostMapping
    public String alterar(@Valid @ModelAttribute("alterarSenha") AlterarSenhaForm form,
                          BindingResult resultado,
                          Authentication authentication,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        if (form.getNovaSenha() != null && !form.getNovaSenha().equals(form.getConfirmarSenha())) {
            resultado.rejectValue("confirmarSenha", "senha.confirmacao", "A confirmação não corresponde à nova senha.");
        }
        if (resultado.hasErrors()) {
            model.addAttribute("alteracaoObrigatoria", false);
            return "conta/senha";
        }
        try {
            usuarioService.alterarMinhaSenha(authentication.getName(), form.getSenhaAtual(), form.getNovaSenha());
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "senha.invalida", ex.getMessage());
            model.addAttribute("alteracaoObrigatoria", false);
            return "conta/senha";
        }
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?senhaAtualizada";
    }
}
