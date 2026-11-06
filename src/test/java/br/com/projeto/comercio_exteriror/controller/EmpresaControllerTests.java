package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.service.EmpresaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTests {

    @Mock
    private EmpresaService empresaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = standaloneSetup(new EmpresaController(empresaService)).build();
    }

    @Test
    void formularioComNomeEmBrancoRetornaErroSemSalvar() throws Exception {
        mockMvc.perform(post("/empresas")
                        .param("nome", "   ")
                        .param("status", "true"))
                .andExpect(view().name("empresas/formulario"))
                .andExpect(model().attributeHasFieldErrors("empresa", "nome"));

        verifyNoInteractions(empresaService);
    }

    @Test
    void formularioValidoSalvaEmpresaERedireciona() throws Exception {
        when(empresaService.salvar(any(Empresa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        mockMvc.perform(post("/empresas")
                        .param("nome", "  Empresa Exemplo  ")
                        .param("status", "true"))
                .andExpect(redirectedUrl("/empresas"))
                .andExpect(flash().attribute("mensagem", "Empresa cadastrada com sucesso."));

        verify(empresaService).salvar(any(Empresa.class));
    }
}
