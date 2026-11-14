package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.IndicadorMensal;
import br.com.projeto.comercio_exteriror.repository.IndicadorMensalRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class IndicadorMensalService {
    private final IndicadorMensalRepository repository;

    public IndicadorMensalService(IndicadorMensalRepository repository) { this.repository = repository; }
    public List<IndicadorMensal> listarTodos() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(IndicadorMensal::getMesAno,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public ResumoDashboard obterResumoDashboard(List<IndicadorMensal> indicadores) {
        long totalOperacoes = indicadores.stream()
                .map(IndicadorMensal::getTotalOperacoes).filter(Objects::nonNull)
                .mapToLong(Integer::longValue).sum();
        double pesoTotal = indicadores.stream().map(IndicadorMensal::getPesoTotalKg)
                .filter(Objects::nonNull).mapToDouble(Float::doubleValue).sum();
        double custoTotal = indicadores.stream().map(IndicadorMensal::getCustoLogisticoTotal)
                .filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        double transitoMedio = mediaPonderada(indicadores, IndicadorMensal::getTempoMedioTransDias, totalOperacoes);
        double desembaracoMedio = mediaPonderada(indicadores, IndicadorMensal::getTempoMedioDesDias, totalOperacoes);
        double percentualAtrasos = mediaPonderada(indicadores, IndicadorMensal::getPercentualAtraso, totalOperacoes);
        return new ResumoDashboard(totalOperacoes, pesoTotal, custoTotal, transitoMedio,
                desembaracoMedio, percentualAtrasos, indicadores.size());
    }

    private double mediaPonderada(List<IndicadorMensal> indicadores,
            java.util.function.Function<IndicadorMensal, ? extends Number> valor, long totalOperacoes) {
        if (totalOperacoes == 0) return 0;
        return indicadores.stream().filter(i -> i.getTotalOperacoes() != null && valor.apply(i) != null)
                .mapToDouble(i -> valor.apply(i).doubleValue() * i.getTotalOperacoes())
                .sum() / totalOperacoes;
    }

    public record ResumoDashboard(long totalOperacoes, double pesoTotal, double custoTotal,
                                  double transitoMedio, double desembaracoMedio,
                                  double percentualAtrasos, int quantidadeIndicadores) { }
}
