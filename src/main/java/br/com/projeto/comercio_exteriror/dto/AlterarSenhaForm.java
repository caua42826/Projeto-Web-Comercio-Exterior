package br.com.projeto.comercio_exteriror.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlterarSenhaForm {
    @NotBlank
    private String senhaAtual;

    @NotBlank @Size(min = 10, max = 72)
    private String novaSenha;

    @NotBlank
    private String confirmarSenha;
}
