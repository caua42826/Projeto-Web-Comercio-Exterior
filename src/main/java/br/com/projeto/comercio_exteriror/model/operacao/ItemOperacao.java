
package br.com.projeto.comercio_exteriror.model.operacao;

import br.com.projeto.comercio_exteriror.model.Produto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "itens_operacao")
@Getter
@Setter
@NoArgsConstructor
public class ItemOperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Integer id;

    @Column(name = "id_origem_planilha", unique = true)
    private Integer idOrigemPlanilha;

    @Column(name = "quantidade")
    private Float quantidade;

    @Column(name = "valor_unit_usd")
    private Double valorUnitUSD;

    @Column(name = "peso_total_kg")
    private Float pesoTotalKg;

    @Column(name = "aliquota_imposto")
    private Double aliquotaImposto;

    @Column(name = "valor_imposto_estimado")
    private Double valorImpostoEstimado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_operacao", nullable = false)
    private Operacao operacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;
}
