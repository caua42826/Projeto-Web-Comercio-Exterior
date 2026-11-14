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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "username", nullable = false, length = 60)
    private String username;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "deve_alterar_senha", nullable = false)
    private boolean deveAlterarSenha;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuarios_perfis",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_perfil"))
    private Set<Perfil> perfis = new LinkedHashSet<>();

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }
}
