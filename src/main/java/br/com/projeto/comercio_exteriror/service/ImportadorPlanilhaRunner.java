package br.com.projeto.comercio_exteriror.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/** Só executa quando a importação for habilitada explicitamente na linha de comando. */
@Component
@ConditionalOnProperty(name = "app.importacao.planilha.executar", havingValue = "true")
@Order(Ordered.LOWEST_PRECEDENCE)
public class ImportadorPlanilhaRunner implements CommandLineRunner {
    private final ImportadorPlanilhaService importador;
    private final String caminho;

    public ImportadorPlanilhaRunner(ImportadorPlanilhaService importador,
            @org.springframework.beans.factory.annotation.Value("${app.importacao.planilha.caminho:}") String caminho) {
        this.importador = importador;
        this.caminho = caminho;
    }

    @Override
    public void run(String... args) throws Exception {
        if (caminho.isBlank()) {
            throw new IllegalArgumentException("Informe app.importacao.planilha.caminho para executar a importação.");
        }
        int registros = importador.importar(Path.of(caminho));
        System.out.printf("Importação concluída. Registros processados: %d%n", registros);
    }
}
