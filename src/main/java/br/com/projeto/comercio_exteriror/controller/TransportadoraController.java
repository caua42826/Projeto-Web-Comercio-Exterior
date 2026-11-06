package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.Transportadora;
import br.com.projeto.comercio_exteriror.service.TransportadoraService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transportadoras")
public class TransportadoraController {
    private final TransportadoraService service;
    public TransportadoraController(TransportadoraService service) { this.service = service; }

    @InitBinder("transportadora")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("nome", "modalPrincipal", "paisSede", "prazoMedioDias", "avaliacaoServico", "status");
    }

    @GetMapping
    public String listar(Model model) { model.addAttribute("transportadoras", service.listarTodas()); return "transportadoras/lista"; }

    @GetMapping("/nova")
    public String nova(Model model) {
        Transportadora t = new Transportadora(); t.setStatus(true);
        model.addAttribute("transportadora", t); model.addAttribute("titulo", "Nova transportadora");
        return "transportadoras/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute("transportadora") Transportadora t, BindingResult r, Model model, RedirectAttributes flash) {
        validar(t, r);
        if (r.hasErrors()) { model.addAttribute("titulo", "Nova transportadora"); return "transportadoras/formulario"; }
        t.setId(null); normalizar(t); service.salvar(t);
        flash.addFlashAttribute("mensagem", "Transportadora cadastrada com sucesso."); return "redirect:/transportadoras";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        return service.buscarPorId(id).map(t -> {
            model.addAttribute("transportadora", t); model.addAttribute("titulo", "Editar transportadora"); return "transportadoras/formulario";
        }).orElseGet(() -> { flash.addFlashAttribute("erro", "Transportadora não encontrada."); return "redirect:/transportadoras"; });
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @ModelAttribute("transportadora") Transportadora dados,
                            BindingResult r, Model model, RedirectAttributes flash) {
        validar(dados, r);
        if (r.hasErrors()) { dados.setId(id); model.addAttribute("titulo", "Editar transportadora"); return "transportadoras/formulario"; }
        return service.buscarPorId(id).map(t -> {
            copiar(dados, t); service.salvar(t); flash.addFlashAttribute("mensagem", "Transportadora atualizada."); return "redirect:/transportadoras";
        }).orElseGet(() -> { flash.addFlashAttribute("erro", "Transportadora não encontrada."); return "redirect:/transportadoras"; });
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Integer id, RedirectAttributes flash) {
        if (service.desativar(id).isPresent()) flash.addFlashAttribute("mensagem", "Transportadora desativada; o histórico foi preservado.");
        else flash.addFlashAttribute("erro", "Transportadora não encontrada.");
        return "redirect:/transportadoras";
    }

    private void validar(Transportadora t, BindingResult r) {
        if (t.getNome() == null || t.getNome().isBlank()) r.rejectValue("nome", "obrigatorio", "Informe o nome da transportadora.");
        else if (t.getNome().length() > 150) r.rejectValue("nome", "tamanho", "O nome pode ter até 150 caracteres.");
        if (t.getModalPrincipal() != null && t.getModalPrincipal().length() > 50) r.rejectValue("modalPrincipal", "tamanho", "O modal pode ter até 50 caracteres.");
        if (t.getPaisSede() != null && t.getPaisSede().length() > 100) r.rejectValue("paisSede", "tamanho", "O país pode ter até 100 caracteres.");
        if (t.getStatus() == null) r.rejectValue("status", "obrigatorio", "Selecione o status.");
        if (t.getPrazoMedioDias() != null && t.getPrazoMedioDias() < 0) r.rejectValue("prazoMedioDias", "naoNegativo", "O prazo não pode ser negativo.");
    }
    private void normalizar(Transportadora t) {
        t.setNome(t.getNome().trim()); t.setModalPrincipal(limpar(t.getModalPrincipal())); t.setPaisSede(limpar(t.getPaisSede()));
    }
    private void copiar(Transportadora o, Transportadora d) {
        d.setNome(o.getNome().trim()); d.setModalPrincipal(limpar(o.getModalPrincipal())); d.setPaisSede(limpar(o.getPaisSede()));
        d.setPrazoMedioDias(o.getPrazoMedioDias()); d.setAvaliacaoServico(o.getAvaliacaoServico()); d.setStatus(o.getStatus());
    }
    private String limpar(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
