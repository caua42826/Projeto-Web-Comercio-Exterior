package br.com.projeto.comercio_exteriror.dto;

import java.util.List;

public record PerfilResumo(Integer id, String codigo, String nome, String descricao,
                           boolean ativo, List<String> permissoes) { }
