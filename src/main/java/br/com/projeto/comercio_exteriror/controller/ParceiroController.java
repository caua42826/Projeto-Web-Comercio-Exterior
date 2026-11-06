package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.Parceiro;
import br.com.projeto.comercio_exteriror.service.ParceiroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/parceiros")
public class ParceiroController {
    private final ParceiroService service;

    public ParceiroController(ParceiroService service) { this.service = service; }

    @InitBinder("parceiro")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("nome", "pais", "continente", "tipo", "setor", "avaliacaoConfiabilidade", "status");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("parceiros", service.listarTodos());
        return "parceiros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Parceiro parceiro = new Parceiro();
        parceiro.setStatus(true);
        model.addAttribute("parceiro", parceiro);
        model.addAttribute("titulo", "Novo parceiro");
        return "parceiros/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute("parceiro") Parceiro parceiro, BindingResult resultado,
                        Model model, RedirectAttributes flash) {
        validar(parceiro, resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("titulo", "Novo parceiro");
            return "parceiros/formulario";
        }
        parceiro.setId(null);
        normalizar(parceiro);
        service.salvar(parceiro);
        flash.addFlashAttribute("mensagem", "Parceiro cadastrado com sucesso.");
        return "redirect:/parceiros";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        return service.buscarPorId(id).map(parceiro -> {
            model.addAttribute("parceiro", parceiro);
            model.addAttribute("titulo", "Editar parceiro");
            return "parceiros/formulario";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erro", "Parceiro não encontrado.");
            return "redirect:/parceiros";
        });
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @ModelAttribute("parceiro") Parceiro dados,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        validar(dados, resultado);
        if (resultado.hasErrors()) {
            dados.setId(id);
            model.addAttribute("titulo", "Editar parceiro");
            return "parceiros/formulario";
        }
        return service.buscarPorId(id).map(parceiro -> {
            copiar(dados, parceiro);
            service.salvar(parceiro);
            flash.addFlashAttribute("mensagem", "Parceiro atualizado com sucesso.");
            return "redirect:/parceiros";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erro", "Parceiro não encontrado.");
            return "redirect:/parceiros";
        });
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Integer id, RedirectAttributes flash) {
        if (service.desativar(id).isPresent()) flash.addFlashAttribute("mensagem", "Parceiro desativado; o histórico foi preservado.");
        else flash.addFlashAttribute("erro", "Parceiro não encontrado.");
        return "redirect:/parceiros";
    }

    private void validar(Parceiro p, BindingResult r) {
        texto(p.getNome(), 150, "nome", "Nome", r, true);
        texto(p.getPais(), 100, "pais", "País", r, false);
        texto(p.getContinente(), 100, "continente", "Continente", r, false);
        texto(p.getTipo(), 100, "tipo", "Tipo", r, false);
        texto(p.getSetor(), 100, "setor", "Setor", r, false);
        if (p.getStatus() == null) r.rejectValue("status", "obrigatorio", "Selecione o status.");
    }

    private void texto(String valor, int max, String campo, String rotulo, BindingResult r, boolean obrigatorio) {
        if (obrigatorio && (valor == null || valor.isBlank())) r.rejectValue(campo, "obrigatorio", rotulo + " é obrigatório.");
        else if (valor != null && valor.length() > max) r.rejectValue(campo, "tamanho", rotulo + " deve ter no máximo " + max + " caracteres.");
    }

    private void normalizar(Parceiro p) {
        p.setNome(p.getNome().trim()); p.setPais(limpar(p.getPais())); p.setContinente(limpar(p.getContinente()));
        p.setTipo(limpar(p.getTipo())); p.setSetor(limpar(p.getSetor()));
    }

    private void copiar(Parceiro origem, Parceiro destino) {
        destino.setNome(origem.getNome().trim()); destino.setPais(limpar(origem.getPais()));
        destino.setContinente(limpar(origem.getContinente())); destino.setTipo(limpar(origem.getTipo()));
        destino.setSetor(limpar(origem.getSetor())); destino.setAvaliacaoConfiabilidade(origem.getAvaliacaoConfiabilidade());
        destino.setStatus(origem.getStatus());
    }

    private String limpar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
