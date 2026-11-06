
package br.com.projeto.comercio_exteriror.model;

import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parceiros")
@Getter
@Setter
@NoArgsConstructor
public class Parceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parceiro")
    private Integer id;

    @Column(name = "id_origem_planilha", unique = true)
    private Integer idOrigemPlanilha;

    @Column(name = "nome_parceiro", nullable = false, length = 150)
    private String nome;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "continente", length = 100)
    private String continente;

    @Column(name = "tipo_parceiro", length = 100)
    private String tipo;

    @Column(name = "setor", length = 100)
    private String setor;

    @Column(name = "avaliacao_confiabilidade")
    private Float avaliacaoConfiabilidade;

    @Column(name = "status_parceiro", nullable = false)
    private Boolean status;

    @OneToMany(mappedBy = "parceiro", fetch = FetchType.LAZY)
    private java.util.List<Operacao> operacoes =
            new java.util.ArrayList<>();

}
