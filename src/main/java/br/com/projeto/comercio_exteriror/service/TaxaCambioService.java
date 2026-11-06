package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.TaxaCambio;
import br.com.projeto.comercio_exteriror.repository.TaxaCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TaxaCambioService {
    private final TaxaCambioRepository repository;

    public TaxaCambioService(TaxaCambioRepository repository) { this.repository = repository; }
    public List<TaxaCambio> listarTodas() { return repository.findAll(); }
    public Optional<TaxaCambio> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    public TaxaCambio salvar(TaxaCambio taxaCambio) { return repository.save(taxaCambio); }
}
