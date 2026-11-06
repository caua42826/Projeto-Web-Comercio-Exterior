package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.enums.StatusOperacao;
import br.com.projeto.comercio_exteriror.enums.TipoOperacao;
import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.model.Documento;
import br.com.projeto.comercio_exteriror.model.Parceiro;
import br.com.projeto.comercio_exteriror.model.Produto;
import br.com.projeto.comercio_exteriror.model.Transportadora;
import br.com.projeto.comercio_exteriror.model.custo.CustoArmazenagem;
import br.com.projeto.comercio_exteriror.model.custo.CustoLogistico;
import br.com.projeto.comercio_exteriror.model.custo.CustoPortuario;
import br.com.projeto.comercio_exteriror.model.operacao.ItemOperacao;
import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import br.com.projeto.comercio_exteriror.model.operacao.PrazosOperacao;
import br.com.projeto.comercio_exteriror.model.operacao.RotaOperacao;
import br.com.projeto.comercio_exteriror.model.operacao.ValoresOperacao;
import br.com.projeto.comercio_exteriror.service.EmpresaService;
import br.com.projeto.comercio_exteriror.service.OperacaoService;
import br.com.projeto.comercio_exteriror.service.ParceiroService;
import br.com.projeto.comercio_exteriror.service.ProdutoService;
import br.com.projeto.comercio_exteriror.service.TransportadoraService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/operacoes")
public class OperacaoController {
    private final OperacaoService operacaoService;
    private final EmpresaService empresaService;
    private final ParceiroService parceiroService;
    private final TransportadoraService transportadoraService;
    private final ProdutoService produtoService;

    public OperacaoController(OperacaoService operacaoService, EmpresaService empresaService,
                              ParceiroService parceiroService, TransportadoraService transportadoraService,
                              ProdutoService produtoService) {
        this.operacaoService = operacaoService;
        this.empresaService = empresaService;
        this.parceiroService = parceiroService;
        this.transportadoraService = transportadoraService;
        this.produtoService = produtoService;
    }

    @InitBinder("operacao")
    public void configurarFormulario(WebDataBinder binder) {
        binder.setAllowedFields("data", "tipo", "incoterm", "moeda", "pesoBrutoKg", "quantidadeVolumes", "precoTotalKG", "status",
                "empresa.id", "parceiro.id", "transportadora.id",
                "prazos.diasTransito", "prazos.diasDesembaraco", "prazos.atrasoDias",
                "rota.paisOrigem", "rota.paisDestino", "rota.idPorto", "rota.modal", "rota.canalDesembaraco", "rota.portoEntradaSaida",
                "valores.valorFrete", "valores.valorSeguro");
    }

    @InitBinder("item")
    public void configurarItem(WebDataBinder binder) {
        binder.setAllowedFields("produto.id", "quantidade", "valorUnitUSD", "pesoTotalKg", "aliquotaImposto", "valorImpostoEstimado");
    }

    @InitBinder("documento")
    public void configurarDocumento(WebDataBinder binder) {
        binder.setAllowedFields("tipoDocumento", "dataEmissao", "dataRecebimento", "status", "diasParaRegularizacao", "possuiDivergencia");
    }

    @InitBinder("custo")
    public void configurarCusto(WebDataBinder binder) {
        binder.setAllowedFields("mesAno", "custoPortuario.despacho", "custoPortuario.demurrage", "custoPortuario.taxasPortuarias",
                "custoArmazenagem.armazenagem", "custoArmazenagem.capatazia", "custoArmazenagem.transporteInterno");
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("operacoes", operacaoService.listarTodas());
        return "operacoes/lista";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        return operacaoService.buscarPorId(id).map(operacao -> {
            prepararVinculosFormulario(operacao);
            CustoLogistico custo = operacao.getCustoLogistico() == null ? novoCusto() : operacao.getCustoLogistico();
            if (custo.getCustoPortuario() == null) custo.setCustoPortuario(new CustoPortuario());
            if (custo.getCustoArmazenagem() == null) custo.setCustoArmazenagem(new CustoArmazenagem());
            prepararDetalhes(model, operacao, new ItemOperacao(), novoDocumento(), custo);
            return "operacoes/detalhes";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erro", "Operação não encontrada.");
            return "redirect:/operacoes";
        });
    }

    @PostMapping("/{id}/itens")
    public String adicionarItem(@PathVariable Integer id, @ModelAttribute("item") ItemOperacao item,
                                BindingResult resultado, Model model, RedirectAttributes flash) {
        Integer idProduto = item.getProduto() == null ? null : item.getProduto().getId();
        Produto produto = idProduto == null ? null : produtoService.buscarPorId(idProduto).orElse(null);
        if (produto == null) resultado.rejectValue("produto.id", "obrigatorio", "Selecione um produto cadastrado.");
        else if (!Boolean.TRUE.equals(produto.getStatus())) resultado.rejectValue("produto.id", "inativo", "Selecione um produto ativo.");
        if (item.getQuantidade() == null || item.getQuantidade() <= 0) resultado.rejectValue("quantidade", "positivo", "A quantidade deve ser maior que zero.");
        if (item.getValorUnitUSD() != null && item.getValorUnitUSD() < 0) resultado.rejectValue("valorUnitUSD", "naoNegativo", "O valor não pode ser negativo.");
        if (item.getPesoTotalKg() != null && item.getPesoTotalKg() < 0) resultado.rejectValue("pesoTotalKg", "naoNegativo", "O peso não pode ser negativo.");
        if (item.getAliquotaImposto() != null && item.getAliquotaImposto() < 0) resultado.rejectValue("aliquotaImposto", "naoNegativo", "A alíquota não pode ser negativa.");
        if (item.getValorImpostoEstimado() != null && item.getValorImpostoEstimado() < 0) resultado.rejectValue("valorImpostoEstimado", "naoNegativo", "O imposto não pode ser negativo.");
        if (resultado.hasErrors()) return renderizarDetalhesComErro(id, model, item, novoDocumento(), novoCusto());
        item.setProduto(produto);
        if (operacaoService.adicionarItem(id, item).isPresent()) flash.addFlashAttribute("mensagem", "Item adicionado à operação.");
        else flash.addFlashAttribute("erro", "Operação não encontrada.");
        return "redirect:/operacoes/" + id;
    }

    @PostMapping("/{id}/itens/{idItem}/remover")
    public String removerItem(@PathVariable Integer id, @PathVariable Integer idItem, RedirectAttributes flash) {
        if (operacaoService.removerItem(id, idItem).isPresent()) flash.addFlashAttribute("mensagem", "Item removido da operação.");
        else flash.addFlashAttribute("erro", "Operação não encontrada.");
        return "redirect:/operacoes/" + id;
    }

    @PostMapping("/{id}/documentos")
    public String adicionarDocumento(@PathVariable Integer id, @ModelAttribute("documento") Documento documento,
                                     BindingResult resultado, Model model, RedirectAttributes flash) {
        if (documento.getTipoDocumento() == null || documento.getTipoDocumento().isBlank()) resultado.rejectValue("tipoDocumento", "obrigatorio", "Informe o tipo de documento.");
        else if (documento.getTipoDocumento().length() > 100) resultado.rejectValue("tipoDocumento", "tamanho", "O tipo deve ter até 100 caracteres.");
        if (documento.getStatus() == null) resultado.rejectValue("status", "obrigatorio", "Informe se o documento foi recebido.");
        if (documento.getPossuiDivergencia() == null) resultado.rejectValue("possuiDivergencia", "obrigatorio", "Informe se há divergência.");
        if (documento.getDataEmissao() != null && documento.getDataRecebimento() != null
                && documento.getDataRecebimento().isBefore(documento.getDataEmissao())) {
            resultado.rejectValue("dataRecebimento", "dataAnterior", "O recebimento não pode anteceder a emissão.");
        }
        if (documento.getDiasParaRegularizacao() != null && documento.getDiasParaRegularizacao() < 0) resultado.rejectValue("diasParaRegularizacao", "naoNegativo", "Os dias não podem ser negativos.");
        if (resultado.hasErrors()) return renderizarDetalhesComErro(id, model, new ItemOperacao(), documento, novoCusto());
        if (operacaoService.adicionarDocumento(id, documento).isPresent()) flash.addFlashAttribute("mensagem", "Documento vinculado à operação.");
        else flash.addFlashAttribute("erro", "Operação não encontrada.");
        return "redirect:/operacoes/" + id;
    }

    @PostMapping("/{id}/documentos/{idDocumento}/remover")
    public String removerDocumento(@PathVariable Integer id, @PathVariable Integer idDocumento, RedirectAttributes flash) {
        if (operacaoService.removerDocumento(id, idDocumento).isPresent()) flash.addFlashAttribute("mensagem", "Documento removido da operação.");
        else flash.addFlashAttribute("erro", "Operação não encontrada.");
        return "redirect:/operacoes/" + id;
    }

    @PostMapping("/{id}/custo-logistico")
    public String salvarCusto(@PathVariable Integer id, @ModelAttribute("custo") CustoLogistico custo,
                              BindingResult resultado, Model model, RedirectAttributes flash) {
        if (custo.getMesAno() == null) resultado.rejectValue("mesAno", "obrigatorio", "Informe o mês de referência.");
        validarCustos(custo, resultado);
        if (resultado.hasErrors()) return renderizarDetalhesComErro(id, model, new ItemOperacao(), novoDocumento(), custo);
        if (operacaoService.salvarCustoLogistico(id, custo).isPresent()) flash.addFlashAttribute("mensagem", "Custos logísticos salvos.");
        else flash.addFlashAttribute("erro", "Operação não encontrada.");
        return "redirect:/operacoes/" + id;
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        Operacao operacao = new Operacao();
        operacao.setPrazos(new PrazosOperacao());
        operacao.setRota(new RotaOperacao());
        operacao.setValores(new ValoresOperacao());
        prepararFormulario(model, operacao, "Nova operação");
        return "operacoes/formulario";
    }

    @PostMapping
    public String criar(@ModelAttribute("operacao") Operacao operacao, BindingResult resultado,
                        Model model, RedirectAttributes flash) {
        validar(operacao, resultado);
        Empresa empresa = resolverEmpresa(operacao, resultado);
        Parceiro parceiro = resolverParceiro(operacao, resultado);
        Transportadora transportadora = resolverTransportadora(operacao, resultado);
        if (resultado.hasErrors()) {
            prepararFormulario(model, operacao, "Nova operação");
            return "operacoes/formulario";
        }

        operacao.setId(null);
        operacao.setEmpresa(empresa);
        operacao.setParceiro(parceiro);
        operacao.setTransportadora(transportadora);
        operacaoService.salvar(operacao);
        flash.addFlashAttribute("mensagem", "Operação cadastrada com sucesso.");
        return "redirect:/operacoes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes flash) {
        return operacaoService.buscarPorId(id).map(operacao -> {
            prepararFormulario(model, operacao, "Editar operação");
            return "operacoes/formulario";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erro", "Operação não encontrada.");
            return "redirect:/operacoes";
        });
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Integer id, @ModelAttribute("operacao") Operacao dados,
                            BindingResult resultado, Model model, RedirectAttributes flash) {
        validar(dados, resultado);
        Empresa empresa = resolverEmpresa(dados, resultado);
        Parceiro parceiro = resolverParceiro(dados, resultado);
        Transportadora transportadora = resolverTransportadora(dados, resultado);
        if (resultado.hasErrors()) {
            dados.setId(id);
            prepararFormulario(model, dados, "Editar operação");
            return "operacoes/formulario";
        }

        return operacaoService.buscarPorId(id).map(operacao -> {
            copiarCampos(dados, operacao);
            operacao.setEmpresa(empresa);
            operacao.setParceiro(parceiro);
            operacao.setTransportadora(transportadora);
            operacaoService.salvar(operacao);
            flash.addFlashAttribute("mensagem", "Operação atualizada com sucesso.");
            return "redirect:/operacoes";
        }).orElseGet(() -> {
            flash.addFlashAttribute("erro", "Operação não encontrada.");
            return "redirect:/operacoes";
        });
    }

    private void prepararFormulario(Model model, Operacao operacao, String titulo) {
        prepararVinculosFormulario(operacao);
        model.addAttribute("operacao", operacao);
        model.addAttribute("titulo", titulo);
        model.addAttribute("tipos", TipoOperacao.values());
        model.addAttribute("statusList", StatusOperacao.values());
        model.addAttribute("empresas", empresaService.listarTodas());
        model.addAttribute("parceiros", parceiroService.listarTodos());
        model.addAttribute("transportadoras", transportadoraService.listarTodas());
    }

    private void prepararDetalhes(Model model, Operacao operacao, ItemOperacao item,
                                  Documento documento, CustoLogistico custo) {
        if (item.getProduto() == null) item.setProduto(new Produto());
        model.addAttribute("operacao", operacao);
        model.addAttribute("item", item);
        model.addAttribute("documento", documento);
        model.addAttribute("custo", custo);
        model.addAttribute("produtos", produtoService.listarTodos().stream()
                .filter(produto -> Boolean.TRUE.equals(produto.getStatus())).toList());
    }

    private String renderizarDetalhesComErro(Integer id, Model model, ItemOperacao item,
                                             Documento documento, CustoLogistico custo) {
        return operacaoService.buscarPorId(id).map(operacao -> {
            prepararDetalhes(model, operacao, item, documento, custo);
            return "operacoes/detalhes";
        }).orElse("redirect:/operacoes");
    }

    private Documento novoDocumento() {
        Documento documento = new Documento();
        documento.setStatus(false);
        documento.setPossuiDivergencia(false);
        return documento;
    }

    private CustoLogistico novoCusto() {
        CustoLogistico custo = new CustoLogistico();
        custo.setCustoPortuario(new CustoPortuario());
        custo.setCustoArmazenagem(new CustoArmazenagem());
        return custo;
    }

    private void prepararVinculosFormulario(Operacao operacao) {
        if (operacao.getPrazos() == null) operacao.setPrazos(new PrazosOperacao());
        if (operacao.getRota() == null) operacao.setRota(new RotaOperacao());
        if (operacao.getValores() == null) operacao.setValores(new ValoresOperacao());
        if (operacao.getEmpresa() == null) operacao.setEmpresa(new Empresa());
        if (operacao.getParceiro() == null) operacao.setParceiro(new Parceiro());
        if (operacao.getTransportadora() == null) operacao.setTransportadora(new Transportadora());
    }

    private void validar(Operacao o, BindingResult r) {
        if (o.getData() == null) r.rejectValue("data", "obrigatorio", "Informe a data da operação.");
        if (o.getTipo() == null) r.rejectValue("tipo", "obrigatorio", "Selecione o tipo de operação.");
        if (o.getStatus() == null) r.rejectValue("status", "obrigatorio", "Selecione o status da operação.");
        if (o.getPesoBrutoKg() != null && o.getPesoBrutoKg() < 0) r.rejectValue("pesoBrutoKg", "naoNegativo", "O peso não pode ser negativo.");
        if (o.getQuantidadeVolumes() != null && o.getQuantidadeVolumes() < 0) r.rejectValue("quantidadeVolumes", "naoNegativo", "A quantidade não pode ser negativa.");
        texto(o.getIncoterm(), 20, "incoterm", "Incoterm", r);
        texto(o.getMoeda(), 10, "moeda", "Moeda", r);
        if (o.getPrecoTotalKG() != null && o.getPrecoTotalKG() < 0) r.rejectValue("precoTotalKG", "naoNegativo", "O preço não pode ser negativo.");
        if (o.getPrazos() != null) {
            if (o.getPrazos().getDiasTransito() != null && o.getPrazos().getDiasTransito() < 0) r.rejectValue("prazos.diasTransito", "naoNegativo", "O prazo não pode ser negativo.");
            if (o.getPrazos().getDiasDesembaraco() != null && o.getPrazos().getDiasDesembaraco() < 0) r.rejectValue("prazos.diasDesembaraco", "naoNegativo", "O prazo não pode ser negativo.");
            if (o.getPrazos().getAtrasoDias() != null && o.getPrazos().getAtrasoDias() < 0) r.rejectValue("prazos.atrasoDias", "naoNegativo", "O atraso não pode ser negativo.");
        }
        if (o.getValores() != null) {
            if (o.getValores().getValorFrete() != null && o.getValores().getValorFrete() < 0) r.rejectValue("valores.valorFrete", "naoNegativo", "O frete não pode ser negativo.");
            if (o.getValores().getValorSeguro() != null && o.getValores().getValorSeguro() < 0) r.rejectValue("valores.valorSeguro", "naoNegativo", "O seguro não pode ser negativo.");
        }
        if (o.getRota() != null) {
            texto(o.getRota().getPaisOrigem(), 100, "rota.paisOrigem", "País de origem", r);
            texto(o.getRota().getPaisDestino(), 100, "rota.paisDestino", "País de destino", r);
            texto(o.getRota().getModal(), 50, "rota.modal", "Modal", r);
            texto(o.getRota().getCanalDesembaraco(), 100, "rota.canalDesembaraco", "Canal", r);
            texto(o.getRota().getPortoEntradaSaida(), 150, "rota.portoEntradaSaida", "Porto", r);
        }
    }

    private void texto(String valor, int max, String campo, String rotulo, BindingResult r) {
        if (valor != null && valor.length() > max) r.rejectValue(campo, "tamanho", rotulo + " deve ter até " + max + " caracteres.");
    }

    private void validarCustos(CustoLogistico custo, BindingResult r) {
        if (custo.getCustoPortuario() != null) {
            if (custo.getCustoPortuario().getDespacho() != null && custo.getCustoPortuario().getDespacho() < 0) r.rejectValue("custoPortuario.despacho", "naoNegativo", "O valor não pode ser negativo.");
            if (custo.getCustoPortuario().getDemurrage() != null && custo.getCustoPortuario().getDemurrage() < 0) r.rejectValue("custoPortuario.demurrage", "naoNegativo", "O valor não pode ser negativo.");
            if (custo.getCustoPortuario().getTaxasPortuarias() != null && custo.getCustoPortuario().getTaxasPortuarias() < 0) r.rejectValue("custoPortuario.taxasPortuarias", "naoNegativo", "O valor não pode ser negativo.");
        }
        if (custo.getCustoArmazenagem() != null) {
            if (custo.getCustoArmazenagem().getArmazenagem() != null && custo.getCustoArmazenagem().getArmazenagem() < 0) r.rejectValue("custoArmazenagem.armazenagem", "naoNegativo", "O valor não pode ser negativo.");
            if (custo.getCustoArmazenagem().getCapatazia() != null && custo.getCustoArmazenagem().getCapatazia() < 0) r.rejectValue("custoArmazenagem.capatazia", "naoNegativo", "O valor não pode ser negativo.");
            if (custo.getCustoArmazenagem().getTransporteInterno() != null && custo.getCustoArmazenagem().getTransporteInterno() < 0) r.rejectValue("custoArmazenagem.transporteInterno", "naoNegativo", "O valor não pode ser negativo.");
        }
    }

    private Empresa resolverEmpresa(Operacao o, BindingResult r) {
        Integer id = o.getEmpresa() == null ? null : o.getEmpresa().getId();
        if (id == null) { r.rejectValue("empresa.id", "obrigatorio", "Selecione a empresa."); return null; }
        return empresaService.buscarPorId(id).orElseGet(() -> { r.rejectValue("empresa.id", "invalido", "Empresa não encontrada."); return null; });
    }

    private Parceiro resolverParceiro(Operacao o, BindingResult r) {
        Integer id = o.getParceiro() == null ? null : o.getParceiro().getId();
        if (id == null) { r.rejectValue("parceiro.id", "obrigatorio", "Selecione o parceiro."); return null; }
        return parceiroService.buscarPorId(id).orElseGet(() -> { r.rejectValue("parceiro.id", "invalido", "Parceiro não encontrado."); return null; });
    }

    private Transportadora resolverTransportadora(Operacao o, BindingResult r) {
        Integer id = o.getTransportadora() == null ? null : o.getTransportadora().getId();
        if (id == null) { r.rejectValue("transportadora.id", "obrigatorio", "Selecione a transportadora."); return null; }
        return transportadoraService.buscarPorId(id).orElseGet(() -> { r.rejectValue("transportadora.id", "invalido", "Transportadora não encontrada."); return null; });
    }

    private void copiarCampos(Operacao origem, Operacao destino) {
        destino.setData(origem.getData()); destino.setTipo(origem.getTipo());
        destino.setIncoterm(limpar(origem.getIncoterm())); destino.setMoeda(limpar(origem.getMoeda()));
        destino.setPesoBrutoKg(origem.getPesoBrutoKg()); destino.setQuantidadeVolumes(origem.getQuantidadeVolumes());
        destino.setPrecoTotalKG(origem.getPrecoTotalKG()); destino.setStatus(origem.getStatus());
        destino.setPrazos(origem.getPrazos()); destino.setRota(origem.getRota()); destino.setValores(origem.getValores());
    }

    private String limpar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
