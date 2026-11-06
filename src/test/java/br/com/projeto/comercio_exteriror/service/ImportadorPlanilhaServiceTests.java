package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.model.operacao.ItemOperacao;
import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import br.com.projeto.comercio_exteriror.enums.StatusOperacao;
import br.com.projeto.comercio_exteriror.repository.EmpresaRepository;
import br.com.projeto.comercio_exteriror.repository.ItemOperacaoRepository;
import br.com.projeto.comercio_exteriror.repository.OperacaoRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:importador_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
class ImportadorPlanilhaServiceTests {
    @Autowired private ImportadorPlanilhaService importador;
    @Autowired private EmpresaRepository empresas;
    @Autowired private OperacaoRepository operacoes;
    @Autowired private ItemOperacaoRepository itens;

    @TempDir Path pastaTemporaria;

    @Test
    void importaRelacionamentosEAtualizaPelosIdsDeOrigemSemDuplicar() throws Exception {
        Path arquivo = pastaTemporaria.resolve("amostra.xlsx");
        criarPlanilhaMinima(arquivo);

        assertThat(importador.importar(arquivo)).isEqualTo(9);
        assertThat(importador.importar(arquivo)).isEqualTo(9);

        assertThat(empresas.count()).isEqualTo(1);
        assertThat(operacoes.count()).isEqualTo(1);
        assertThat(itens.count()).isEqualTo(1);
        Empresa empresa = empresas.findAll().getFirst();
        assertThat(empresa.getIdOrigemPlanilha()).isEqualTo(10);
        assertThat(empresa.getStatus()).isFalse();
        Operacao operacao = operacoes.findAll().getFirst();
        assertThat(operacao.getIdOrigemPlanilha()).isEqualTo(40);
        assertThat(operacao.getStatus()).isEqualTo(StatusOperacao.ATRASADA);
        assertThat(operacao.getEmpresa().getId()).isEqualTo(empresa.getId());
        ItemOperacao item = itens.findAll().getFirst();
        assertThat(item.getIdOrigemPlanilha()).isEqualTo(70);
        assertThat(item.getOperacao().getId()).isEqualTo(operacao.getId());
    }

    private void criarPlanilhaMinima(Path arquivo) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            adicionar(wb, "empresas", new String[]{"id_empresa", "nome_empresa", "status_empresa"},
                    new Object[]{10, "Empresa amostra", "Inativa"});
            adicionar(wb, "parceiros", new String[]{"id_parceiro", "nome_parceiro", "status_parceiro"},
                    new Object[]{20, "Parceiro amostra", "Ativo"});
            adicionar(wb, "produtos", new String[]{"id_produto", "descricao_produto", "necessita_licenca", "status_produto"},
                    new Object[]{30, "Produto amostra", "Não", "Ativo"});
            adicionar(wb, "transportadoras", new String[]{"id_transportadora", "nome_transportadora", "status_transportadora"},
                    new Object[]{35, "Transportadora amostra", "Ativa"});
            adicionar(wb, "taxas_cambio", new String[]{"id_cambio", "ano", "mes", "moeda", "taxa_media_brl"},
                    new Object[]{36, 2024, 1, "USD", 4.9});
            adicionar(wb, "operacoes", new String[]{"id_operacao", "data_operacao", "tipo_operacao", "id_empresa", "id_parceiro", "pais_origem", "pais_destino", "modal", "incoterm", "moeda", "valor_frete", "valor_seguro", "peso_bruto_kg", "quantidade_volumes", "status_operacao", "dias_transito", "dias_desembaraco", "atraso_dias"},
                    new Object[]{40, 45292, "Importação", 10, 20, "Brasil", "Chile", "Marítimo", "FOB", "USD", 120, 15, 200, 2, "Atrasada", 10, 2, 1});
            adicionar(wb, "itens_operacao", new String[]{"id_item_operacao", "id_operacao", "id_produto", "quantidade", "valor_unitario", "peso_total_kg", "aliquota_imposto", "valor_imposto_estimado"},
                    new Object[]{70, 40, 30, 2, 50, 20, 0.1, 10});
            adicionar(wb, "documentos", new String[]{"id_documento", "id_operacao", "tipo_documento", "data_emissao", "data_recebimento", "status_documento", "dias_para_regularizacao", "possui_divergencia"},
                    new Object[]{80, 40, "Fatura", 45292, 45293, "Regular", 0, "Não"});
            adicionar(wb, "custos_logisticos", new String[]{"id_custo", "id_operacao", "ano", "mes", "armazenagem", "capatazia", "despachante", "demurrage", "taxas_portuarias", "transporte_interno"},
                    new Object[]{90, 40, 2024, 1, 10, 2, 3, 0, 4, 1});
            adicionar(wb, "indicadores_mensais", new String[]{"id_indicador", "ano", "mes", "total_operacoes", "peso_total_kg", "tempo_medio_transito_dias", "tempo_medio_desembaraco_dias", "percentual_atrasos", "custo_logistico_total"},
                    null);
            try (OutputStream out = Files.newOutputStream(arquivo)) { wb.write(out); }
        }
    }

    private void adicionar(XSSFWorkbook wb, String nome, String[] cabecalhos, Object[] valores) {
        Sheet sheet = wb.createSheet(nome);
        Row header = sheet.createRow(0);
        for (int i = 0; i < cabecalhos.length; i++) header.createCell(i).setCellValue(cabecalhos[i]);
        if (valores != null) {
            Row row = sheet.createRow(1);
            for (int i = 0; i < valores.length; i++) {
                Object valor = valores[i];
                if (valor instanceof Number numero) row.createCell(i).setCellValue(numero.doubleValue());
                else row.createCell(i).setCellValue(String.valueOf(valor));
            }
        }
    }
}
