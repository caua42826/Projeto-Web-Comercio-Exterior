package br.com.projeto.comercio_exteriror.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class UsuarioForm {
    @NotBlank @Size(max = 150)
    private String nomeCompleto;

    @NotBlank @Size(min = 3, max = 60)
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Use apenas letras, números, ponto, hífen e sublinhado.")
    private String username;

    @NotBlank @Email @Size(max = 180)
    private String email;

    @Size(max = 72, message = "A senha não pode exceder 72 caracteres.")
    private String senha;

    @NotEmpty(message = "Selecione ao menos um perfil.")
    private Set<Integer> perfisIds = new LinkedHashSet<>();

    private boolean ativo = true;
}
