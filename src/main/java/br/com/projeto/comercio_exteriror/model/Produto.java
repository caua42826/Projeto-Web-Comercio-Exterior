
package br.com.projeto.comercio_exteriror.model;

import br.com.projeto.comercio_exteriror.model.operacao.ItemOperacao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer id;

    @Column(name = "descricao_produto", nullable = false, length = 255)
    private String descricao;

    @Column(name = "categoria_produto", length = 100)
    private String categoria;

    @Column(name = "ncm_sintetico", length = 20)
    private String ncmSintetico;

    @Column(name = "unidade_medida", length = 20)
    private String unidadeMedida;

    @Column(name = "peso_unitario_kg")
    private Float pesoUnitKg;

    @Column(name = "valor_unitario_referencia_usd")
    private Double valorUnRefUSD;

    @Column(name = "necessita_licenca", nullable = false)
    private Boolean necessitaLicenca;

    @Column(name = "status_produto", nullable = false)
    private Boolean status;

    @OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
    private java.util.List<ItemOperacao> itensOperacao =
            new java.util.ArrayList<>();

}
