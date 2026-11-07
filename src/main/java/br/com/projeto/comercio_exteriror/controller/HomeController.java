package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.service.DashboardService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("contagens", dashboardService.obterContagens());
        return "inicio";
    }
}
