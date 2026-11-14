package br.com.projeto.comercio_exteriror.dto;

public record PermissaoResumo(Integer id, String codigo, String nome, String descricao,
                              boolean ativo, boolean deSistema) { }
