package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.enums.StatusOperacao;
import br.com.projeto.comercio_exteriror.enums.TipoOperacao;
import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.model.Parceiro;
import br.com.projeto.comercio_exteriror.model.Produto;
import br.com.projeto.comercio_exteriror.model.Transportadora;
import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import br.com.projeto.comercio_exteriror.repository.EmpresaRepository;
import br.com.projeto.comercio_exteriror.repository.OperacaoRepository;
import br.com.projeto.comercio_exteriror.repository.ParceiroRepository;
import br.com.projeto.comercio_exteriror.repository.ProdutoRepository;
import br.com.projeto.comercio_exteriror.repository.TransportadoraRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:comercio_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
@WithMockUser
class TelasMvcTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ParceiroRepository parceiroRepository;
    @Autowired private TransportadoraRepository transportadoraRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private OperacaoRepository operacaoRepository;

    @Test
    void paginasPrincipaisRenderizam() throws Exception {
        String[] paginas = {
                "/",
                "/empresas",
                "/empresas/nova",
                "/parceiros",
                "/parceiros/novo",
                "/produtos",
                "/produtos/novo",
                "/transportadoras",
                "/transportadoras/nova",
                "/taxas-cambio",
                "/taxas-cambio/nova",
                "/indicadores",
                "/operacoes",
                "/operacoes/nova"
        };

        for (String pagina : paginas) {
            mockMvc.perform(get(pagina)).andExpect(status().isOk());
        }
    }

    @Test
    @Transactional
    void operacaoAgrupaItensDocumentosECustos() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setNome("Empresa teste");
        empresa.setStatus(true);
        empresa = empresaRepository.save(empresa);

        Parceiro parceiro = new Parceiro();
        parceiro.setNome("Parceiro teste");
        parceiro.setStatus(true);
        parceiro = parceiroRepository.save(parceiro);

        Transportadora transportadora = new Transportadora();
        transportadora.setNome("Transportadora teste");
        transportadora.setStatus(true);
        transportadora = transportadoraRepository.save(transportadora);

        Produto produto = new Produto();
        produto.setDescricao("Produto teste");
        produto.setNecessitaLicenca(false);
        produto.setStatus(true);
        produto = produtoRepository.save(produto);

        mockMvc.perform(post("/operacoes").with(csrf())
                        .param("data", "2026-10-15")
                        .param("tipo", TipoOperacao.IMPORTACAO.name())
                        .param("status", StatusOperacao.EM_TRANSITO.name())
                        .param("empresa.id", empresa.getId().toString())
                        .param("parceiro.id", parceiro.getId().toString())
                        .param("transportadora.id", transportadora.getId().toString())
                        .param("prazos.diasTransito", "12")
                        .param("rota.paisOrigem", "Brasil")
                        .param("valores.valorFrete", "120.50"))
                .andExpect(redirectedUrl("/operacoes"));

        Operacao operacao = operacaoRepository.findAll().getFirst();
        Integer idOperacao = operacao.getId();

        mockMvc.perform(post("/operacoes/{id}/itens", idOperacao).with(csrf())
                        .param("produto.id", produto.getId().toString())
                        .param("quantidade", "2")
                        .param("valorUnitUSD", "50.00"))
                .andExpect(redirectedUrl("/operacoes/" + idOperacao));

        mockMvc.perform(post("/operacoes/{id}/documentos", idOperacao).with(csrf())
                        .param("tipoDocumento", "Fatura comercial")
                        .param("dataEmissao", "2026-10-15")
                        .param("status", "true")
                        .param("possuiDivergencia", "false"))
                .andExpect(redirectedUrl("/operacoes/" + idOperacao));

        mockMvc.perform(post("/operacoes/{id}/custo-logistico", idOperacao).with(csrf())
                        .param("mesAno", "2026-10-01")
                        .param("custoPortuario.despacho", "100.00")
                        .param("custoArmazenagem.armazenagem", "50.00"))
                .andExpect(redirectedUrl("/operacoes/" + idOperacao));

        mockMvc.perform(get("/operacoes/{id}", idOperacao)).andExpect(status().isOk());
    }
}
