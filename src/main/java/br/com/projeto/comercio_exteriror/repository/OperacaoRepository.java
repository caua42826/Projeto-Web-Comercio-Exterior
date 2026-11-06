package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacaoRepository extends JpaRepository<Operacao, Integer> {
}
