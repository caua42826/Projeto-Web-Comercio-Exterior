package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.TaxaCambio;
import br.com.projeto.comercio_exteriror.repository.TaxaCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class TaxaCambioService {
    private final TaxaCambioRepository repository;

    public TaxaCambioService(TaxaCambioRepository repository) { this.repository = repository; }
    public List<TaxaCambio> listarTodas() { return repository.findAll(); }
    public Optional<TaxaCambio> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public TaxaCambio salvar(TaxaCambio taxaCambio) { return repository.save(taxaCambio); }
}
