package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.Produto;
import br.com.projeto.comercio_exteriror.service.ProdutoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoService service;
    public ProdutoController(ProdutoService service) { this.service = service; }

    @InitBinder("produto")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("descricao", "categoria", "ncmSintetico", "unidadeMedida", "pesoUnitKg", "valorUnRefUSD", "necessitaLicenca", "status");
    }

    @GetMapping
    public String listar(Model model) { model.addAttribute("produtos", service.listarTodos()); return "produtos/lista"; }

    @GetMapping("/novo")
    public String novo(Model model) {
        Produto produto = new Produto(); produto.setStatus(true); produto.setNecessitaLicenca(false);
        model.addAttribute("produto", produto); model.addAttribute("titulo", "Novo produto");
        return "produtos/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute("produto") Produto produto, BindingResult resultado, Model model, RedirectAttributes flash) {
        validar(produto, resultado);
        if (resultado.hasErrors()) { model.addAttribute("titulo", "Novo produto"); return "produtos/formulario"; }
        produto.setId(null); normalizar(produto); service.salvar(produto);
        flash.addFlashAttribute("mensagem", "Produto cadastrado com sucesso."); return "redirect:/produtos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        return service.buscarPorId(id).map(produto -> {
            model.addAttribute("produto", produto); model.addAttribute("titulo", "Editar produto"); return "produtos/formulario";
        }).orElseGet(() -> { flash.addFlashAttribute("erro", "Produto não encontrado."); return "redirect:/produtos"; });
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @ModelAttribute("produto") Produto dados, BindingResult resultado,
                            Model model, RedirectAttributes flash) {
        validar(dados, resultado);
        if (resultado.hasErrors()) { dados.setId(id); model.addAttribute("titulo", "Editar produto"); return "produtos/formulario"; }
        return service.buscarPorId(id).map(produto -> {
            copiar(dados, produto); service.salvar(produto); flash.addFlashAttribute("mensagem", "Produto atualizado."); return "redirect:/produtos";
        }).orElseGet(() -> { flash.addFlashAttribute("erro", "Produto não encontrado."); return "redirect:/produtos"; });
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Integer id, RedirectAttributes flash) {
        if (service.desativar(id).isPresent()) flash.addFlashAttribute("mensagem", "Produto desativado; o histórico foi preservado.");
        else flash.addFlashAttribute("erro", "Produto não encontrado.");
        return "redirect:/produtos";
    }

    private void validar(Produto p, BindingResult r) {
        texto(p.getDescricao(), 255, "descricao", "Descrição", r, true);
        texto(p.getCategoria(), 100, "categoria", "Categoria", r);
        texto(p.getNcmSintetico(), 20, "ncmSintetico", "NCM", r);
        texto(p.getUnidadeMedida(), 20, "unidadeMedida", "Unidade de medida", r);
        if (p.getNecessitaLicenca() == null) r.rejectValue("necessitaLicenca", "obrigatorio", "Informe se exige licença.");
        if (p.getStatus() == null) r.rejectValue("status", "obrigatorio", "Selecione o status.");
        if (p.getPesoUnitKg() != null && p.getPesoUnitKg() < 0) r.rejectValue("pesoUnitKg", "naoNegativo", "O peso não pode ser negativo.");
        if (p.getValorUnRefUSD() != null && p.getValorUnRefUSD() < 0) r.rejectValue("valorUnRefUSD", "naoNegativo", "O valor não pode ser negativo.");
    }

    private void texto(String v, int max, String field, String label, BindingResult r) { texto(v, max, field, label, r, false); }
    private void texto(String v, int max, String field, String label, BindingResult r, boolean required) {
        if (required && (v == null || v.isBlank())) r.rejectValue(field, "obrigatorio", label + " é obrigatória.");
        else if (v != null && v.length() > max) r.rejectValue(field, "tamanho", label + " deve ter no máximo " + max + " caracteres.");
    }

    private void normalizar(Produto p) {
        p.setDescricao(p.getDescricao().trim()); p.setCategoria(limpar(p.getCategoria()));
        p.setNcmSintetico(limpar(p.getNcmSintetico())); p.setUnidadeMedida(limpar(p.getUnidadeMedida()));
    }

    private void copiar(Produto o, Produto d) {
        d.setDescricao(o.getDescricao().trim()); d.setCategoria(limpar(o.getCategoria()));
        d.setNcmSintetico(limpar(o.getNcmSintetico())); d.setUnidadeMedida(limpar(o.getUnidadeMedida()));
        d.setPesoUnitKg(o.getPesoUnitKg()); d.setValorUnRefUSD(o.getValorUnRefUSD());
        d.setNecessitaLicenca(o.getNecessitaLicenca()); d.setStatus(o.getStatus());
    }
    private String limpar(String v) { return v == null || v.isBlank() ? null : v.trim(); }
}
