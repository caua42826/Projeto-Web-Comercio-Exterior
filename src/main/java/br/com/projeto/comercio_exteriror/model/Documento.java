
package br.com.projeto.comercio_exteriror.model;

import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer id;

    @Column(name = "tipo_documento", length = 100)
    private String tipoDocumento;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_recebimento")
    private LocalDate dataRecebimento;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "dias_para_regularizacao")
    private Integer diasParaRegularizacao;

    @Column(name = "possui_divergencia")
    private Boolean possuiDivergencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_operacao", nullable = false)
    private Operacao operacao;
}
