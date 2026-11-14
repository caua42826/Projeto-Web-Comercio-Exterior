package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.dto.PerfilForm;
import br.com.projeto.comercio_exteriror.service.PerfilService;
import br.com.projeto.comercio_exteriror.service.RegraSegurancaException;
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
@RequestMapping("/admin/perfis")
public class PerfilAdminController {
    private final PerfilService perfilService;

    public PerfilAdminController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @InitBinder("perfil")
    public void protegerCamposFormulario(WebDataBinder binder) {
        binder.setAllowedFields("codigo", "nome", "descricao", "permissoesIds", "ativo");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("perfis", perfilService.listar());
        return "admin/perfis/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new PerfilForm());
        return "admin/perfis/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("perfil") PerfilForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, form);
            return "admin/perfis/formulario";
        }
        try {
            perfilService.criar(form);
            redirect.addFlashAttribute("mensagem", "Perfil criado.");
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "perfil.invalido", ex.getMessage());
            prepararFormulario(model, form);
            return "admin/perfis/formulario";
        }
        return "redirect:/admin/perfis";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            prepararFormulario(model, perfilService.buscarFormulario(id));
            model.addAttribute("perfilId", id);
            return "admin/perfis/formulario";
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/admin/perfis";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @Valid @ModelAttribute("perfil") PerfilForm form,
                            BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, form);
            model.addAttribute("perfilId", id);
            return "admin/perfis/formulario";
        }
        try {
            perfilService.atualizar(id, form);
            redirect.addFlashAttribute("mensagem", "Perfil atualizado.");
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "perfil.invalido", ex.getMessage());
            prepararFormulario(model, form);
            model.addAttribute("perfilId", id);
            return "admin/perfis/formulario";
        }
        return "redirect:/admin/perfis";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            perfilService.excluir(id);
            redirect.addFlashAttribute("mensagem", "Perfil excluído.");
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/perfis";
    }

    private void prepararFormulario(Model model, PerfilForm form) {
        model.addAttribute("perfil", form);
        model.addAttribute("permissoes", perfilService.listarPermissoes());
    }
}
