package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.IndicadorMensal;
import br.com.projeto.comercio_exteriror.repository.IndicadorMensalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class IndicadorMensalService {
    private final IndicadorMensalRepository repository;

    public IndicadorMensalService(IndicadorMensalRepository repository) { this.repository = repository; }
    public List<IndicadorMensal> listarTodos() { return repository.findAll(); }
}
