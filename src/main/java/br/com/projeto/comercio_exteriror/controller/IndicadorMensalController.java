package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.service.IndicadorMensalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/indicadores")
public class IndicadorMensalController {
    private final IndicadorMensalService service;
    public IndicadorMensalController(IndicadorMensalService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        var indicadores = service.listarTodos();
        model.addAttribute("indicadores", indicadores);
        model.addAttribute("resumo", service.obterResumoDashboard(indicadores));
        return "indicadores/lista";
    }
}
