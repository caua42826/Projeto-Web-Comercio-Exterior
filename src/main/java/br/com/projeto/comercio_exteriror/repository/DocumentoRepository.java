package br.com.projeto.comercio_exteriror.repository;

import br.com.projeto.comercio_exteriror.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<Documento, Integer> {
}
