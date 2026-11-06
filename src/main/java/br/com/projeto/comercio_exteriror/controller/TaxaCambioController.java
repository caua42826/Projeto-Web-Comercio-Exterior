package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.TaxaCambio;
import br.com.projeto.comercio_exteriror.service.TaxaCambioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/taxas-cambio")
public class TaxaCambioController {
    private final TaxaCambioService service;
    public TaxaCambioController(TaxaCambioService service) { this.service = service; }

    @InitBinder("taxaCambio")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("moeda", "taxaMediaBRL", "mesAno");
    }

    @GetMapping
    public String listar(Model model) { model.addAttribute("taxas", service.listarTodas()); return "taxas-cambio/lista"; }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("taxaCambio", new TaxaCambio()); model.addAttribute("titulo", "Nova taxa de câmbio");
        return "taxas-cambio/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute("taxaCambio") TaxaCambio taxa, BindingResult resultado, Model model, RedirectAttributes flash) {
        validar(taxa, resultado);
        if (resultado.hasErrors()) { model.addAttribute("titulo", "Nova taxa de câmbio"); return "taxas-cambio/formulario"; }
        taxa.setId(null); normalizar(taxa); service.salvar(taxa);
        flash.addFlashAttribute("mensagem", "Taxa de câmbio cadastrada."); return "redirect:/taxas-cambio";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        return service.buscarPorId(id).map(taxa -> {
            model.addAttribute("taxaCambio", taxa); model.addAttribute("titulo", "Editar taxa de câmbio"); return "taxas-cambio/formulario";
        }).orElseGet(() -> { flash.addFlashAttribute("erro", "Taxa de câmbio não encontrada."); return "redirect:/taxas-cambio"; });
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @ModelAttribute("taxaCambio") TaxaCambio dados,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        validar(dados, resultado);
        if (resultado.hasErrors()) { dados.setId(id); model.addAttribute("titulo", "Editar taxa de câmbio"); return "taxas-cambio/formulario"; }
        return service.buscarPorId(id).map(taxa -> {
            taxa.setMoeda(dados.getMoeda().trim().toUpperCase()); taxa.setTaxaMediaBRL(dados.getTaxaMediaBRL());
            taxa.setMesAno(dados.getMesAno().withDayOfMonth(1));
            service.salvar(taxa); flash.addFlashAttribute("mensagem", "Taxa de câmbio atualizada."); return "redirect:/taxas-cambio";
        }).orElseGet(() -> { flash.addFlashAttribute("erro", "Taxa de câmbio não encontrada."); return "redirect:/taxas-cambio"; });
    }

    private void validar(TaxaCambio taxa, BindingResult r) {
        if (taxa.getMoeda() == null || taxa.getMoeda().isBlank()) r.rejectValue("moeda", "obrigatorio", "Informe a moeda.");
        else if (taxa.getMoeda().length() > 10) r.rejectValue("moeda", "tamanho", "A moeda deve ter até 10 caracteres.");
        if (taxa.getTaxaMediaBRL() == null || taxa.getTaxaMediaBRL() <= 0) r.rejectValue("taxaMediaBRL", "positivo", "Informe uma taxa maior que zero.");
        if (taxa.getMesAno() == null) r.rejectValue("mesAno", "obrigatorio", "Informe o mês de referência.");
    }

    private void normalizar(TaxaCambio taxa) {
        taxa.setMoeda(taxa.getMoeda().trim().toUpperCase());
        taxa.setMesAno(taxa.getMesAno().withDayOfMonth(1));
    }
}
