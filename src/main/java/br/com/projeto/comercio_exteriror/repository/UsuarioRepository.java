package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("select distinct u from Usuario u left join fetch u.perfis p " +
            "left join fetch p.permissoes where lower(u.username) = lower(:username)")
    Optional<Usuario> carregarParaAutenticacao(@Param("username") String username);

    List<Usuario> findAllByOrderByNomeCompletoAsc();

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByAtivoTrueAndPerfis_CodigoIgnoreCase(String codigo);

    long countByAtivoTrueAndPerfis_CodigoIgnoreCase(String codigo);

    long countByPerfis_Id(Integer id);

    long countByAtivoTrue();
}
