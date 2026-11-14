package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.dto.PermissaoForm;
import br.com.projeto.comercio_exteriror.service.PermissaoService;
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
@RequestMapping("/admin/permissoes")
public class PermissaoAdminController {
    private final PermissaoService permissaoService;

    public PermissaoAdminController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @InitBinder("permissao")
    public void protegerCamposFormulario(WebDataBinder binder) {
        binder.setAllowedFields("codigo", "nome", "descricao", "ativo");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("permissoes", permissaoService.listar());
        return "admin/permissoes/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("permissao", new PermissaoForm());
        return "admin/permissoes/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("permissao") PermissaoForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) return "admin/permissoes/formulario";
        try {
            permissaoService.criar(form);
            redirect.addFlashAttribute("mensagem", "Permissão criada.");
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "permissao.invalida", ex.getMessage());
            return "admin/permissoes/formulario";
        }
        return "redirect:/admin/permissoes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("permissao", permissaoService.buscarFormulario(id));
            model.addAttribute("permissaoId", id);
            return "admin/permissoes/formulario";
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/admin/permissoes";
        }
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @Valid @ModelAttribute("permissao") PermissaoForm form,
                            BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("permissaoId", id);
            return "admin/permissoes/formulario";
        }
        try {
            permissaoService.atualizar(id, form);
            redirect.addFlashAttribute("mensagem", "Permissão atualizada.");
        } catch (RegraSegurancaException ex) {
            resultado.rejectValue(ex.getCampo(), "permissao.invalida", ex.getMessage());
            model.addAttribute("permissaoId", id);
            return "admin/permissoes/formulario";
        }
        return "redirect:/admin/permissoes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            permissaoService.excluir(id);
            redirect.addFlashAttribute("mensagem", "Permissão excluída.");
        } catch (RegraSegurancaException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/admin/permissoes";
    }
}
