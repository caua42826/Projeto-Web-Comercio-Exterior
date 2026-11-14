package br.com.projeto.comercio_exteriror.security;

import java.util.List;

public final class PermissoesSistema {
    public static final String DADOS_CONSULTAR = "DADOS_CONSULTAR";
    public static final String DADOS_GERENCIAR = "DADOS_GERENCIAR";
    public static final String USUARIOS_GERENCIAR = "USUARIOS_GERENCIAR";
    public static final String PERFIS_GERENCIAR = "PERFIS_GERENCIAR";
    public static final String PERMISSOES_GERENCIAR = "PERMISSOES_GERENCIAR";

    public static final List<Definicao> DEFINICOES = List.of(
            new Definicao(DADOS_CONSULTAR, "Consultar dados", "Visualizar cadastros e indicadores."),
            new Definicao(DADOS_GERENCIAR, "Gerenciar dados", "Criar, editar e desativar registros."),
            new Definicao(USUARIOS_GERENCIAR, "Gerenciar usuários", "Administrar contas de acesso."),
            new Definicao(PERFIS_GERENCIAR, "Gerenciar perfis", "Administrar perfis e seus vínculos."),
            new Definicao(PERMISSOES_GERENCIAR, "Gerenciar permissões", "Administrar permissões disponíveis."));

    private PermissoesSistema() { }

    public record Definicao(String codigo, String nome, String descricao) { }
}
