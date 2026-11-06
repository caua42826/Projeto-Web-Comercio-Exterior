package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.operacao.ItemOperacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemOperacaoRepository extends JpaRepository<ItemOperacao, Integer> {
}
