
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
public class PrazosOperacao {

    @Column(name = "dias_transito")
    private Integer diasTransito;

    @Column(name = "dias_desembaraco")
    private Integer diasDesembaraco;

    @Column(name = "atraso_dias")
    private Integer atrasoDias;
}
