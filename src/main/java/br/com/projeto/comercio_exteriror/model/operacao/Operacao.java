
package br.com.projeto.comercio_exteriror.model.operacao;

import br.com.projeto.comercio_exteriror.model.custo.CustoLogistico;
import br.com.projeto.comercio_exteriror.model.Documento;

import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.model.Parceiro;
import br.com.projeto.comercio_exteriror.model.Transportadora;
import br.com.projeto.comercio_exteriror.enums.StatusOperacao;
import br.com.projeto.comercio_exteriror.enums.TipoOperacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "operacoes")
@Getter
@Setter
@NoArgsConstructor
public class Operacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operacao")
    private Integer id;

    @Column(name = "data_operacao")
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacao", nullable = false)
    private TipoOperacao tipo;

    @Column(name = "incoterm", length = 20)
    private String incoterm;

    @Column(name = "moeda", length = 10)
    private String moeda;

    @Column(name = "peso_bruto_kg")
    private Float pesoBrutoKg;

    @Column(name = "quantidade_volumes")
    private Integer quantidadeVolumes;

    @Column(name = "preco_total_kg")
    private Double precoTotalKG;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_operacao")
    private StatusOperacao status;

    @Embedded
    private PrazosOperacao prazos;

    @Embedded
    private RotaOperacao rota;

    @Embedded
    private ValoresOperacao valores;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parceiro")
    private Parceiro parceiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transportadora")
    private Transportadora transportadora;

    @OneToMany(
            mappedBy = "operacao",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private java.util.List<ItemOperacao> itens =
            new java.util.ArrayList<>();

    @OneToMany(
            mappedBy = "operacao",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private java.util.List<Documento> documentos =
            new java.util.ArrayList<>();

    @OneToOne(mappedBy = "operacao", cascade = CascadeType.ALL)
    private CustoLogistico custoLogistico;

}
