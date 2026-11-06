package br.com.projeto.comercio_exteriror.model.custo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CustoArmazenagem {

    @Column(name = "armazenagem")
    private Float armazenagem;

    @Column(name = "capatazia")
    private Float capatazia;

    @Column(name = "transporte_interno")
    private Float transporteInterno;
}
