package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
}
