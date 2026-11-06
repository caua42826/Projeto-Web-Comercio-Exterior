
package br.com.projeto.comercio_exteriror.model;

import jakarta.persistence.*;
import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transportadoras")
@Getter
@Setter
@NoArgsConstructor
public class Transportadora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportadora")
    private Integer id;

    @Column(name = "nome_transportadora", nullable = false, length = 150)
    private String nome;

    @Column(name = "modal_principal", length = 50)
    private String modalPrincipal;

    @Column(name = "pais_sede", length = 100)
    private String paisSede;

    @Column(name = "prazo_medio_dias")
    private Integer prazoMedioDias;

    @Column(name = "avaliacao_servico")
    private Float avaliacaoServico;

    @Column(name = "status_transportadora", nullable = false)
    private Boolean status;

    @OneToMany(mappedBy = "transportadora", fetch = FetchType.LAZY)
    private java.util.List<Operacao> operacoes = new java.util.ArrayList<>();
}
