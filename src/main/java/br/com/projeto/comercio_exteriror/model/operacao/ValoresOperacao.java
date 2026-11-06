
package br.com.projeto.comercio_exteriror.model.operacao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ValoresOperacao {

    @Column(name = "valor_frete")
    private Double valorFrete;

    @Column(name = "valor_seguro")
    private Double valorSeguro;
}
