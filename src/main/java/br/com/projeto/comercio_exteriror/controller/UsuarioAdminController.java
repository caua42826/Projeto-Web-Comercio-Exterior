package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.dto.RedefinirSenhaForm;
import br.com.projeto.comercio_exteriror.dto.UsuarioForm;
import br.com.projeto.comercio_exteriror.service.PerfilService;
import br.com.projeto.comercio_exteriror.service.RegraSegurancaException;
import br.com.projeto.comercio_exteriror.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {
    private final UsuarioService usuarioService;
    private final PerfilService perfilService;

    public UsuarioAdminController(UsuarioService usuarioService, PerfilService perfilService) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
    }

    @InitBinder("usuario")
    public void protegerCamposFormulario(WebDataBinder binder) {
        binder.setAllowedFields("nomeCompleto", "username", "email", "senha", "perfisIds", "ativo");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "admin/usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new UsuarioForm());
        return "admin/usuarios/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("usuario") UsuarioForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (form.getSenha() == null || form.getSenha().isBlank()) {
            resultado.rejectValue("senha", "senha.obrigatoria", "Defina uma senha inicial para o usuário.");
        }
        if (resultado.hasErrors()) {
            prepararFormulario(model, form);
            return "admin/usuarios/formulario";
        }
        try {
            usuarioService.criar(form);
            redirect.addFlashAttribute("mensagem", "Usuário criado. A senha deverá ser alterada no primeiro acesso.");
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "usuario.invalido", ex.getMessage());
            prepararFormulario(model, form);
            return "admin/usuarios/formulario";
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            prepararFormulario(model, usuarioService.buscarFormulario(id));
            model.addAttribute("usuarioResumo", usuarioService.buscarResumo(id));
            return "admin/usuarios/formulario";
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @Valid @ModelAttribute("usuario") UsuarioForm form,
                            BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, form);
            model.addAttribute("usuarioResumo", usuarioService.buscarResumo(id));
            return "admin/usuarios/formulario";
        }
        try {
            usuarioService.atualizar(id, form);
            redirect.addFlashAttribute("mensagem", "Dados do usuário atualizados.");
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "usuario.invalido", ex.getMessage());
            prepararFormulario(model, form);
            model.addAttribute("usuarioResumo", usuarioService.buscarResumo(id));
            return "admin/usuarios/formulario";
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Integer id, @org.springframework.web.bind.annotation.RequestParam boolean ativo,
                                RedirectAttributes redirect) {
        try {
            usuarioService.alterarStatus(id, ativo);
            redirect.addFlashAttribute("mensagem", ativo ? "Usuário ativado." : "Usuário desativado.");
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/senha")
    public String redefinirSenha(@PathVariable Integer id,
                                 @Valid @ModelAttribute("redefinirSenha") RedefinirSenhaForm form,
                                 BindingResult resultado, RedirectAttributes redirect) {
        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmarSenha())) {
            redirect.addFlashAttribute("erro", "A confirmação da senha não corresponde.");
            return "redirect:/admin/usuarios";
        }
        if (resultado.hasErrors()) {
            redirect.addFlashAttribute("erro", "A senha não atende aos requisitos definidos.");
            return "redirect:/admin/usuarios";
        }
        try {
            usuarioService.redefinirSenha(id, form.getSenha());
            redirect.addFlashAttribute("mensagem", "Senha redefinida. O usuário deverá alterá-la no próximo acesso.");
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    private void prepararFormulario(Model model, UsuarioForm form) {
        model.addAttribute("usuario", form);
        model.addAttribute("perfis", perfilService.listarAtivos());
    }
}
