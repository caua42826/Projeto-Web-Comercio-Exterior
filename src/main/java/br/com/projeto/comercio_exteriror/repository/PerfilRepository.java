package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
    Optional<Perfil> findByCodigoIgnoreCase(String codigo);

    List<Perfil> findAllByOrderByCodigoAsc();

    boolean existsByCodigoIgnoreCase(String codigo);
}
