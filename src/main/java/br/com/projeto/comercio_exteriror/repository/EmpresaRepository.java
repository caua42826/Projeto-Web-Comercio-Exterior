package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
}
