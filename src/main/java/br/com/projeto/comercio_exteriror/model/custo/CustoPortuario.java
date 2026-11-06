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
public class CustoPortuario {

    @Column(name = "despacho")
    private Float despacho;

    @Column(name = "demurrage")
    private Float demurrage;

    @Column(name = "taxas_portuarias")
    private Float taxasPortuarias;
}
