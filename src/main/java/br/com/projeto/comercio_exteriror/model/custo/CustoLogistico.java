
package br.com.projeto.comercio_exteriror.model.custo;

import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "custos_logisticos")
@Getter
@Setter
@NoArgsConstructor
public class CustoLogistico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_custo")
    private Integer id;

    @Column(name = "id_origem_planilha", unique = true)
    private Integer idOrigemPlanilha;

    @Column(name = "mes_ano")
    private LocalDate mesAno;

    @Embedded
    private CustoPortuario custoPortuario;

    @Embedded
    private CustoArmazenagem custoArmazenagem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_operacao", unique = true)
    private Operacao operacao;
}
