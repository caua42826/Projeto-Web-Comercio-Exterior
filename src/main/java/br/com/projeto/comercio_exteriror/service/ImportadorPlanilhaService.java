package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.enums.StatusOperacao;
import br.com.projeto.comercio_exteriror.enums.TipoOperacao;
import br.com.projeto.comercio_exteriror.model.*;
import br.com.projeto.comercio_exteriror.model.custo.CustoArmazenagem;
import br.com.projeto.comercio_exteriror.model.custo.CustoLogistico;
import br.com.projeto.comercio_exteriror.model.custo.CustoPortuario;
import br.com.projeto.comercio_exteriror.model.operacao.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/** Importa as abas do arquivo de origem sem substituir os IDs gerados pelo banco. */
@Service
public class ImportadorPlanilhaService {
    private static final int TAMANHO_LOTE = 500;

    private final EntityManager em;
    private final DataFormatter formatter = new DataFormatter(Locale.US);
    private int pendentes;
    private int importados;

    public ImportadorPlanilhaService(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public int importar(Path arquivo) {
        if (!Files.isRegularFile(arquivo)) {
            throw new IllegalArgumentException("Arquivo da planilha não encontrado: " + arquivo);
        }
        Map<Integer, Integer> empresaIds = indices(Empresa.class);
        Map<Integer, Integer> parceiroIds = indices(Parceiro.class);
        Map<Integer, Integer> produtoIds = indices(Produto.class);
        Map<Integer, Integer> transportadoraIds = indices(Transportadora.class);
        Map<Integer, Integer> taxaIds = indices(TaxaCambio.class);
        Map<Integer, Integer> operacaoIds = indices(Operacao.class);
        Map<Integer, Integer> itemIds = indices(ItemOperacao.class);
        Map<Integer, Integer> documentoIds = indices(Documento.class);
        Map<Integer, Integer> custoIds = indices(CustoLogistico.class);
        Map<Integer, Integer> indicadorIds = indices(IndicadorMensal.class);

        importados = 0;
        pendentes = 0;
        importarSimples(arquivo, "empresas", Empresa.class, Empresa::getId,
                empresaIds, this::preencherEmpresa);
        progresso("empresas", empresaIds.size());
        importarSimples(arquivo, "parceiros", Parceiro.class, Parceiro::getId,
                parceiroIds, this::preencherParceiro);
        progresso("parceiros", parceiroIds.size());
        importarSimples(arquivo, "produtos", Produto.class, Produto::getId,
                produtoIds, this::preencherProduto);
        progresso("produtos", produtoIds.size());
        importarSimples(arquivo, "transportadoras", Transportadora.class, Transportadora::getId,
                transportadoraIds, this::preencherTransportadora);
        progresso("transportadoras", transportadoraIds.size());
        importarSimples(arquivo, "taxas_cambio", TaxaCambio.class, TaxaCambio::getId,
                taxaIds, this::preencherTaxa);
        progresso("taxas_cambio", taxaIds.size());
        importarOperacoes(arquivo, operacaoIds, empresaIds, parceiroIds);
        progresso("operacoes", operacaoIds.size());
        importarItens(arquivo, itemIds, operacaoIds, produtoIds);
        progresso("itens_operacao", itemIds.size());
        importarDocumentos(arquivo, documentoIds, operacaoIds);
        progresso("documentos", documentoIds.size());
        importarCustos(arquivo, custoIds, operacaoIds);
        progresso("custos_logisticos", custoIds.size());
        importarIndicadores(arquivo, indicadorIds);
        progresso("indicadores_mensais", indicadorIds.size());
        em.flush();
        return importados;
    }

    private <T> void importarSimples(Path arquivo, String aba, Class<T> tipo, Function<T, Integer> idLocal,
            Map<Integer, Integer> ids, Preenchedor<T> preenchedor) {
        processarAba(arquivo, aba, linha -> {
            Integer origem = linha.inteiro("id_" + idColuna(aba));
            if (origem == null) return;
            T entidade = obter(tipo, origem, ids, idLocal);
            preenchedor.preencher(entidade, linha);
            guardar(entidade, ids, origem, idLocal);
        });
    }

    private String idColuna(String aba) {
        return switch (aba) {
            case "empresas" -> "empresa";
            case "parceiros" -> "parceiro";
            case "produtos" -> "produto";
            case "transportadoras" -> "transportadora";
            case "taxas_cambio" -> "cambio";
            default -> throw new IllegalArgumentException("Aba não suportada: " + aba);
        };
    }

    private void preencherEmpresa(Empresa e, Linha r) {
        e.setNome(r.texto("nome_empresa")); e.setSetor(r.texto("setor"));
        e.setCidade(r.texto("cidade")); e.setEstado(r.texto("estado"));
        e.setPorte(r.texto("porte_empresa")); e.setTipoAtuacao(r.texto("tipo_atuacao"));
        e.setStatus(ativo(r.texto("status_empresa")));
    }
    private void preencherParceiro(Parceiro e, Linha r) {
        e.setNome(r.texto("nome_parceiro")); e.setPais(r.texto("pais"));
        e.setContinente(r.texto("continente")); e.setTipo(r.texto("tipo_parceiro"));
        e.setSetor(r.texto("setor")); e.setAvaliacaoConfiabilidade(r.decimalFloat("avaliacao_confiabilidade"));
        e.setStatus(ativo(r.texto("status_parceiro")));
    }
    private void preencherProduto(Produto e, Linha r) {
        e.setDescricao(r.texto("descricao_produto")); e.setCategoria(r.texto("categoria_produto"));
        e.setNcmSintetico(r.texto("ncm_sintetico")); e.setUnidadeMedida(r.texto("unidade_medida"));
        e.setPesoUnitKg(r.decimalFloat("peso_unitario_kg"));
        e.setValorUnRefUSD(r.decimal("valor_unitario_referencia_usd"));
        e.setNecessitaLicenca(sim(r.texto("necessita_licenca")));
        e.setStatus(ativo(r.texto("status_produto")));
    }
    private void preencherTransportadora(Transportadora e, Linha r) {
        e.setNome(r.texto("nome_transportadora")); e.setModalPrincipal(r.texto("modal_principal"));
        e.setPaisSede(r.texto("pais_sede")); e.setPrazoMedioDias(r.inteiro("prazo_medio_dias"));
        e.setAvaliacaoServico(r.decimalFloat("avaliacao_servico"));
        e.setStatus(ativo(r.texto("status_transportadora")));
    }
    private void preencherTaxa(TaxaCambio e, Linha r) {
        e.setMoeda(r.texto("moeda")); e.setTaxaMediaBRL(r.decimal("taxa_media_brl"));
        e.setMesAno(LocalDate.of(r.inteiroObrigatorio("ano"), r.inteiroObrigatorio("mes"), 1));
    }

    private void importarOperacoes(Path arquivo, Map<Integer, Integer> ids,
            Map<Integer, Integer> empresaIds, Map<Integer, Integer> parceiroIds) {
        processarAba(arquivo, "operacoes", r -> {
            Integer origem = r.inteiro("id_operacao"); if (origem == null) return;
            Operacao o = obter(Operacao.class, origem, ids, Operacao::getId);
            o.setIdOrigemPlanilha(origem); o.setData(data(r, "data_operacao"));
            o.setTipo(tipoOperacao(r.texto("tipo_operacao")));
            o.setIncoterm(r.texto("incoterm")); o.setMoeda(r.texto("moeda"));
            o.setPesoBrutoKg(r.decimalFloat("peso_bruto_kg"));
            o.setQuantidadeVolumes(r.inteiro("quantidade_volumes"));
            o.setStatus(statusOperacao(r.texto("status_operacao")));
            PrazosOperacao p = new PrazosOperacao();
            p.setDiasTransito(r.inteiro("dias_transito")); p.setDiasDesembaraco(r.inteiro("dias_desembaraco"));
            p.setAtrasoDias(r.inteiro("atraso_dias")); o.setPrazos(p);
            RotaOperacao rota = new RotaOperacao();
            rota.setPaisOrigem(r.texto("pais_origem")); rota.setPaisDestino(r.texto("pais_destino"));
            rota.setIdPorto(r.inteiro("id_porto")); rota.setPortoEntradaSaida(r.texto("porto_entrada_saida"));
            rota.setModal(r.texto("modal")); rota.setCanalDesembaraco(r.texto("canal_desembaraco"));
            o.setRota(rota);
            ValoresOperacao v = new ValoresOperacao();
            v.setValorFrete(r.decimal("valor_frete")); v.setValorSeguro(r.decimal("valor_seguro")); o.setValores(v);
            o.setEmpresa(referencia(Empresa.class, empresaIds, r.inteiroObrigatorio("id_empresa"), "empresa"));
            o.setParceiro(referencia(Parceiro.class, parceiroIds, r.inteiroObrigatorio("id_parceiro"), "parceiro"));
            // A aba não contém id_transportadora; a associação permanece nula intencionalmente.
            guardar(o, ids, origem, Operacao::getId);
        });
    }

    private void importarItens(Path arquivo, Map<Integer, Integer> ids,
            Map<Integer, Integer> operacaoIds, Map<Integer, Integer> produtoIds) {
        processarAba(arquivo, "itens_operacao", r -> {
            Integer origem = r.inteiro("id_item_operacao"); if (origem == null) return;
            ItemOperacao i = obter(ItemOperacao.class, origem, ids, ItemOperacao::getId);
            i.setIdOrigemPlanilha(origem); i.setQuantidade(r.decimalFloat("quantidade"));
            i.setValorUnitUSD(r.decimal("valor_unitario")); i.setPesoTotalKg(r.decimalFloat("peso_total_kg"));
            i.setAliquotaImposto(r.decimal("aliquota_imposto"));
            i.setValorImpostoEstimado(r.decimal("valor_imposto_estimado"));
            i.setOperacao(referencia(Operacao.class, operacaoIds, r.inteiroObrigatorio("id_operacao"), "operação"));
            i.setProduto(referencia(Produto.class, produtoIds, r.inteiroObrigatorio("id_produto"), "produto"));
            guardar(i, ids, origem, ItemOperacao::getId);
        });
    }

    private void importarDocumentos(Path arquivo, Map<Integer, Integer> ids, Map<Integer, Integer> operacaoIds) {
        processarAba(arquivo, "documentos", r -> {
            Integer origem = r.inteiro("id_documento"); if (origem == null) return;
            Documento d = obter(Documento.class, origem, ids, Documento::getId);
            d.setIdOrigemPlanilha(origem); d.setTipoDocumento(r.texto("tipo_documento"));
            d.setDataEmissao(data(r, "data_emissao")); d.setDataRecebimento(data(r, "data_recebimento"));
            d.setStatus(ativo(r.texto("status_documento")));
            d.setDiasParaRegularizacao(r.inteiro("dias_para_regularizacao"));
            d.setPossuiDivergencia(sim(r.texto("possui_divergencia")));
            d.setOperacao(referencia(Operacao.class, operacaoIds, r.inteiroObrigatorio("id_operacao"), "operação"));
            guardar(d, ids, origem, Documento::getId);
        });
    }

    private void importarCustos(Path arquivo, Map<Integer, Integer> ids, Map<Integer, Integer> operacaoIds) {
        processarAba(arquivo, "custos_logisticos", r -> {
            Integer origem = r.inteiro("id_custo"); if (origem == null) return;
            CustoLogistico c = obter(CustoLogistico.class, origem, ids, CustoLogistico::getId);
            c.setIdOrigemPlanilha(origem); c.setMesAno(LocalDate.of(r.inteiroObrigatorio("ano"), r.inteiroObrigatorio("mes"), 1));
            CustoPortuario cp = new CustoPortuario(); cp.setDespacho(r.decimalFloat("despachante"));
            cp.setDemurrage(r.decimalFloat("demurrage")); cp.setTaxasPortuarias(r.decimalFloat("taxas_portuarias"));
            c.setCustoPortuario(cp);
            CustoArmazenagem ca = new CustoArmazenagem(); ca.setArmazenagem(r.decimalFloat("armazenagem"));
            ca.setCapatazia(r.decimalFloat("capatazia")); ca.setTransporteInterno(r.decimalFloat("transporte_interno"));
            c.setCustoArmazenagem(ca);
            c.setOperacao(referencia(Operacao.class, operacaoIds, r.inteiroObrigatorio("id_operacao"), "operação"));
            guardar(c, ids, origem, CustoLogistico::getId);
        });
    }

    private void importarIndicadores(Path arquivo, Map<Integer, Integer> ids) {
        processarAba(arquivo, "indicadores_mensais", r -> {
            Integer origem = r.inteiro("id_indicador"); if (origem == null) return;
            IndicadorMensal i = obter(IndicadorMensal.class, origem, ids, IndicadorMensal::getId);
            i.setIdOrigemPlanilha(origem); i.setMesAno(LocalDate.of(r.inteiroObrigatorio("ano"), r.inteiroObrigatorio("mes"), 1));
            i.setTotalOperacoes(r.inteiro("total_operacoes")); i.setPesoTotalKg(r.decimalFloat("peso_total_kg"));
            i.setTempoMedioTransDias(r.decimalFloat("tempo_medio_transito_dias"));
            i.setTempoMedioDesDias(r.decimalFloat("tempo_medio_desembaraco_dias"));
            i.setPercentualAtraso(r.decimal("percentual_atrasos"));
            i.setCustoLogisticoTotal(r.decimal("custo_logistico_total"));
            guardar(i, ids, origem, IndicadorMensal::getId);
        });
    }

    private <T> T obter(Class<T> tipo, Integer origem, Map<Integer, Integer> ids, Function<T, Integer> idLocal) {
        Integer local = ids.get(origem);
        if (local != null) return em.find(tipo, local);
        try {
            T entidade = tipo.getDeclaredConstructor().newInstance();
            if (tipo == Empresa.class) ((Empresa) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == Parceiro.class) ((Parceiro) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == Produto.class) ((Produto) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == Transportadora.class) ((Transportadora) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == TaxaCambio.class) ((TaxaCambio) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == Operacao.class) ((Operacao) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == ItemOperacao.class) ((ItemOperacao) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == Documento.class) ((Documento) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == CustoLogistico.class) ((CustoLogistico) entidade).setIdOrigemPlanilha(origem);
            else if (tipo == IndicadorMensal.class) ((IndicadorMensal) entidade).setIdOrigemPlanilha(origem);
            return entidade;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Não foi possível criar " + tipo.getSimpleName(), e);
        }
    }

    private <T> void guardar(T entidade, Map<Integer, Integer> ids, Integer origem, Function<T, Integer> idLocal) {
        if (idLocal.apply(entidade) == null) em.persist(entidade);
        else if (!em.contains(entidade)) em.merge(entidade);
        ids.put(origem, idLocal.apply(entidade));
        importados++;
        tocarLote();
    }

    private void tocarLote() {
        pendentes++;
        if (pendentes >= TAMANHO_LOTE) {
            em.flush(); em.clear(); pendentes = 0;
        }
    }

    private void progresso(String aba, int quantidade) {
        System.out.printf("Aba %s processada: %d registros.%n", aba, quantidade);
    }

    private <T> T referencia(Class<T> tipo, Map<Integer, Integer> ids, Integer origem, String entidade) {
        Integer local = ids.get(origem);
        if (local == null) throw new EntityNotFoundException("ID de origem " + origem + " não encontrado para " + entidade);
        return em.getReference(tipo, local);
    }

    private Map<Integer, Integer> indices(Class<?> tipo) {
        List<Object[]> pares = em.createQuery("select e.idOrigemPlanilha, e.id from "
                + tipo.getSimpleName() + " e where e.idOrigemPlanilha is not null", Object[].class).getResultList();
        Map<Integer, Integer> resultado = new HashMap<>();
        for (Object[] par : pares) resultado.put(((Number) par[0]).intValue(), ((Number) par[1]).intValue());
        return resultado;
    }

    private void processarAba(Path arquivo, String nomeAba, java.util.function.Consumer<Linha> consumidor) {
        try (OPCPackage pkg = OPCPackage.open(arquivo.toFile(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            StylesTable styles = reader.getStylesTable();
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream sheetStream = sheets.next()) {
                    if (!nomeAba.equals(sheets.getSheetName())) continue;
                    List<String> cabecalhos = new ArrayList<>();
                    XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                        private Map<Integer, String> valores;
                        @Override public void startRow(int rowNum) { valores = new HashMap<>(); }
                        @Override public void endRow(int rowNum) {
                            if (rowNum == 0) {
                                int maior = valores.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
                                for (int i = 0; i <= maior; i++) cabecalhos.add(valores.getOrDefault(i, "").trim());
                            } else {
                                Map<String, String> linha = new HashMap<>();
                                valores.forEach((indice, valor) -> {
                                    if (indice < cabecalhos.size() && !cabecalhos.get(indice).isBlank())
                                        linha.put(chaveColuna(cabecalhos.get(indice)), valor);
                                });
                                consumidor.accept(new Linha(linha, rowNum + 1));
                            }
                        }
                        @Override public void cell(String ref, String valor, XSSFComment comment) {
                            valores.put((int) new CellReference(ref).getCol(), valor == null ? "" : valor);
                        }
                        @Override public void headerFooter(String text, boolean isHeader, String tagName) { }
                    };
                    XMLReader parser = org.apache.poi.util.XMLHelper.newXMLReader();
                    parser.setContentHandler(new XSSFSheetXMLHandler(styles, null, strings, handler, formatter, false));
                    parser.parse(new InputSource(sheetStream));
                    return;
                }
            }
            throw new IllegalArgumentException("A aba '" + nomeAba + "' não existe na planilha.");
        } catch (Exception e) {
            if (e instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("Falha ao ler a aba '" + nomeAba + "' da planilha.", e);
        }
    }

    private LocalDate data(Linha r, String coluna) {
        String valor = r.texto(coluna);
        if (valor.isBlank()) return null;
        try { return LocalDate.of(1899, 12, 30).plusDays((long) Double.parseDouble(valor)); }
        catch (NumberFormatException ignored) { }
        for (java.time.format.DateTimeFormatter formato : List.of(
                java.time.format.DateTimeFormatter.ofPattern("M/d/yy", Locale.US),
                java.time.format.DateTimeFormatter.ofPattern("M/d/uuuu", Locale.US),
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)) {
            try { return LocalDate.parse(valor, formato); } catch (java.time.DateTimeException ignored) { }
        }
        throw new IllegalArgumentException("Data não reconhecida na coluna " + coluna + ": " + valor);
    }

    private Integer inteiro(String valor) { return valor == null || valor.isBlank() ? null : (int) Math.round(Double.parseDouble(valor)); }
    private String normalizar(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
    }
    private boolean sim(String s) { return Set.of("SIM", "S", "TRUE", "1", "YES").contains(normalizar(s)); }
    private boolean ativo(String s) {
        return Set.of("ATIVA", "ATIVO", "REGULAR", "APROVADO", "ENTREGUE").contains(normalizar(s));
    }
    private TipoOperacao tipoOperacao(String s) {
        return switch (normalizar(s)) {
            case "IMPORTACAO" -> TipoOperacao.IMPORTACAO;
            case "EXPORTACAO" -> TipoOperacao.EXPORTACAO;
            default -> throw new IllegalArgumentException("Tipo de operação não reconhecido: " + s);
        };
    }
    private StatusOperacao statusOperacao(String s) {
        return switch (normalizar(s)) {
            case "ENTREGUE" -> StatusOperacao.ENTREGUE;
            case "EM TRANSITO" -> StatusOperacao.EM_TRANSITO;
            case "ATRASADA" -> StatusOperacao.ATRASADA;
            case "DESEMBARACADA" -> StatusOperacao.DESEMBARACADA;
            case "AGUARDANDO DOCUMENTACAO" -> StatusOperacao.AGUARDANDO_DOCUMENTACAO;
            default -> throw new IllegalArgumentException("Status de operação não reconhecido: " + s);
        };
    }

    @FunctionalInterface private interface Preenchedor<T> { void preencher(T entidade, Linha linha); }

    private class Linha {
        private final Map<String, String> valores;
        private final int numero;
        private Linha(Map<String, String> valores, int numero) { this.valores = valores; this.numero = numero; }
        private String texto(String nome) {
            String valor = valores.get(chaveColuna(nome));
            return valor == null ? "" : valor.trim();
        }
        private Integer inteiro(String nome) { return ImportadorPlanilhaService.this.inteiro(texto(nome)); }
        private Integer inteiroObrigatorio(String nome) {
            Integer v = inteiro(nome); if (v == null) throw new IllegalArgumentException("Campo obrigatório vazio: " + nome + ", linha " + numero); return v;
        }
        private Double decimal(String nome) {
            String s = texto(nome).replace(",", "");
            return s.isEmpty() ? null : Double.valueOf(s);
        }
        private Float decimalFloat(String nome) { Double v = decimal(nome); return v == null ? null : v.floatValue(); }
    }

    private String chaveColuna(String nome) {
        return Normalizer.normalize(nome == null ? "" : nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
