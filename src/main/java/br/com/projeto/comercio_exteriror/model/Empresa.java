
package br.com.projeto.comercio_exteriror.model;

import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer id;

    @Column(name = "nome_empresa", nullable = false, length = 150)
    private String nome;

    @Column(name = "setor", length = 100)
    private String setor;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "estado", length = 100)
    private String estado;

    @Column(name = "porte_empresa", length = 50)
    private String porte;

    @Column(name = "tipo_atuacao", length = 100)
    private String tipoAtuacao;

    @Column(name = "status_empresa", nullable = false)
    private Boolean status;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private java.util.List<Operacao> operacoes =
            new java.util.ArrayList<>();

}
