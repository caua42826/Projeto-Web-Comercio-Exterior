package br.com.projeto.comercio_exteriror.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissaoForm {
    @NotBlank @Size(min = 3, max = 80)
    @Pattern(regexp = "[A-Z][A-Z0-9_]*", message = "Use letras maiúsculas, números ou sublinhado.")
    private String codigo;

    @NotBlank @Size(max = 120)
    private String nome;

    @Size(max = 250)
    private String descricao;

    private boolean ativo = true;
}
