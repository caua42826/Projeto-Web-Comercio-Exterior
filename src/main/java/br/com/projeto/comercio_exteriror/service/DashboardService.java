package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class DashboardService {
    private final OperacaoRepository operacoes;
    private final EmpresaRepository empresas;
    private final ParceiroRepository parceiros;
    private final TransportadoraRepository transportadoras;
    private final ProdutoRepository produtos;
    private final TaxaCambioRepository taxasCambio;
    private final IndicadorMensalRepository indicadores;

    public DashboardService(OperacaoRepository operacoes, EmpresaRepository empresas,
            ParceiroRepository parceiros, TransportadoraRepository transportadoras,
            ProdutoRepository produtos, TaxaCambioRepository taxasCambio,
            IndicadorMensalRepository indicadores) {
        this.operacoes = operacoes;
        this.empresas = empresas;
        this.parceiros = parceiros;
        this.transportadoras = transportadoras;
        this.produtos = produtos;
        this.taxasCambio = taxasCambio;
        this.indicadores = indicadores;
    }

    public Contagens obterContagens() {
        return new Contagens(operacoes.count(), empresas.count(), parceiros.count(),
                transportadoras.count(), produtos.count(), taxasCambio.count(), indicadores.count());
    }

    public record Contagens(long operacoes, long empresas, long parceiros,
                            long transportadoras, long produtos, long taxasCambio,
                            long indicadores) { }
}
