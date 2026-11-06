package br.com.projeto.comercio_exteriror.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Converte os ENUMs nativos antigos do H2 para VARCHAR, compatível com EnumType.STRING. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MigracaoEnumsOperacao implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    public MigracaoEnumsOperacao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void run(ApplicationArguments args) {
        jdbc.execute("ALTER TABLE operacoes ALTER COLUMN tipo_operacao VARCHAR(40)");
        jdbc.execute("ALTER TABLE operacoes ALTER COLUMN status_operacao VARCHAR(40)");
    }
}
