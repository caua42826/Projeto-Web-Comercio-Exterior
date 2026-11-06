package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.service.EmpresaService;
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
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @InitBinder("empresa")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("nome", "setor", "cidade", "estado", "porte", "tipoAtuacao", "status");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empresas", empresaService.listarTodas());
        return "empresas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        Empresa empresa = new Empresa();
        empresa.setStatus(true);
        model.addAttribute("empresa", empresa);
        model.addAttribute("titulo", "Nova empresa");
        return "empresas/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute("empresa") Empresa empresa,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        validar(empresa, resultado);
        if (resultado.hasErrors()) {
            model.addAttribute("titulo", "Nova empresa");
            return "empresas/formulario";
        }

        empresa.setId(null);
        copiarCamposEditaveis(empresa, empresa);
        empresaService.salvar(empresa);
        redirectAttributes.addFlashAttribute("mensagem", "Empresa cadastrada com sucesso.");
        return "redirect:/empresas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        return empresaService.buscarPorId(id)
                .map(empresa -> {
                    model.addAttribute("empresa", empresa);
                    model.addAttribute("titulo", "Editar empresa");
                    return "empresas/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Empresa não encontrada.");
                    return "redirect:/empresas";
                });
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id,
                            @ModelAttribute("empresa") Empresa dadosFormulario,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        validar(dadosFormulario, resultado);
        if (resultado.hasErrors()) {
            dadosFormulario.setId(id);
            model.addAttribute("titulo", "Editar empresa");
            return "empresas/formulario";
        }

        return empresaService.buscarPorId(id)
                .map(empresa -> {
                    copiarCamposEditaveis(dadosFormulario, empresa);
                    empresaService.salvar(empresa);
                    redirectAttributes.addFlashAttribute("mensagem", "Empresa atualizada com sucesso.");
                    return "redirect:/empresas";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Empresa não encontrada.");
                    return "redirect:/empresas";
                });
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        if (empresaService.desativar(id).isPresent()) {
            redirectAttributes.addFlashAttribute("mensagem", "Empresa desativada. O registro foi mantido no histórico.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Empresa não encontrada.");
        }
        return "redirect:/empresas";
    }

    private void validar(Empresa empresa, BindingResult resultado) {
        validarTexto(empresa.getNome(), 150, "nome", "Nome", resultado, true);
        validarTexto(empresa.getSetor(), 100, "setor", "Setor", resultado, false);
        validarTexto(empresa.getCidade(), 100, "cidade", "Cidade", resultado, false);
        validarTexto(empresa.getEstado(), 100, "estado", "Estado", resultado, false);
        validarTexto(empresa.getPorte(), 50, "porte", "Porte", resultado, false);
        validarTexto(empresa.getTipoAtuacao(), 100, "tipoAtuacao", "Tipo de atuação", resultado, false);
        if (empresa.getStatus() == null) {
            resultado.rejectValue("status", "empresa.status.obrigatorio", "Selecione o status da empresa.");
        }
    }

    private void validarTexto(String valor, int tamanhoMaximo, String campo, String rotulo,
                              BindingResult resultado, boolean obrigatorio) {
        if (obrigatorio && (valor == null || valor.isBlank())) {
            resultado.rejectValue(campo, "empresa." + campo + ".obrigatorio", rotulo + " é obrigatório.");
        } else if (valor != null && valor.length() > tamanhoMaximo) {
            resultado.rejectValue(campo, "empresa." + campo + ".tamanho", rotulo + " deve ter no máximo " + tamanhoMaximo + " caracteres.");
        }
    }

    private void copiarCamposEditaveis(Empresa origem, Empresa destino) {
        destino.setNome(origem.getNome().trim());
        destino.setSetor(normalizar(origem.getSetor()));
        destino.setCidade(normalizar(origem.getCidade()));
        destino.setEstado(normalizar(origem.getEstado()));
        destino.setPorte(normalizar(origem.getPorte()));
        destino.setTipoAtuacao(normalizar(origem.getTipoAtuacao()));
        destino.setStatus(origem.getStatus());
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
