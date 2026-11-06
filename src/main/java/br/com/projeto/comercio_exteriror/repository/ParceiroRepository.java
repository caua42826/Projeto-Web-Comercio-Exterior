package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Parceiro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParceiroRepository extends JpaRepository<Parceiro, Integer> {
}
