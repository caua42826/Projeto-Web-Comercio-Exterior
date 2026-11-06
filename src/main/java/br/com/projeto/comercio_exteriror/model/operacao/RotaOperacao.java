
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
public class RotaOperacao {

    @Column(name = "pais_origem", length = 100)
    private String paisOrigem;

    @Column(name = "pais_destino", length = 100)
    private String paisDestino;

    @Column(name = "id_porto")
    private Integer idPorto;

    @Column(name = "modal", length = 50)
    private String modal;

    @Column(name = "canal_desembaraco", length = 100)
    private String canalDesembaraco;

    @Column(name = "porto_entrada_saida", length = 150)
    private String portoEntradaSaida;
}
