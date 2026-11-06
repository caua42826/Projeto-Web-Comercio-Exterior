package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.custo.CustoLogistico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustoLogisticoRepository extends JpaRepository<CustoLogistico, Integer> {
}
