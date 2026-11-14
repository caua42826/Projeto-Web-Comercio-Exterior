package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissaoRepository extends JpaRepository<Permissao, Integer> {
    Optional<Permissao> findByCodigoIgnoreCase(String codigo);

    List<Permissao> findAllByOrderByCodigoAsc();

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByPerfis_Id(Integer id);
}
