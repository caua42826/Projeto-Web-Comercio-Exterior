package br.com.projeto.comercio_exteriror.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResumo(Integer id, String nomeCompleto, String username, String email,
                           boolean ativo, boolean deveAlterarSenha, List<String> perfis,
                           LocalDateTime criadoEm) { }
