package br.com.projeto.comercio_exteriror.dto;

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
public class PerfilForm {
    @NotBlank @Size(min = 3, max = 60)
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]*", message = "O código deve começar com letra e conter apenas letras, números ou sublinhado.")
    private String codigo;

    @NotBlank @Size(max = 100)
    private String nome;

    @Size(max = 250)
    private String descricao;

    private Set<Integer> permissoesIds = new LinkedHashSet<>();

    private boolean ativo = true;
}
