package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.service.AdminDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminDashboardService dashboardService;

    public AdminController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("resumo", dashboardService.obterResumo());
        return "admin/dashboard";
    }
}
