package br.com.projeto.comercio_exteriror.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "perfis", uniqueConstraints =
        @UniqueConstraint(name = "uk_perfis_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer id;

    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", length = 250)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "perfis_permissoes",
            joinColumns = @JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @JoinColumn(name = "id_permissao"))
    private Set<Permissao> permissoes = new LinkedHashSet<>();
}
