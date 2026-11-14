package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.Transportadora;
import br.com.projeto.comercio_exteriror.repository.TransportadoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class TransportadoraService {
    private final TransportadoraRepository repository;

    public TransportadoraService(TransportadoraRepository repository) { this.repository = repository; }
    public List<Transportadora> listarTodas() { return repository.findAll(); }
    public Optional<Transportadora> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Transportadora salvar(Transportadora transportadora) { return repository.save(transportadora); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Optional<Transportadora> desativar(Integer id) {
        return repository.findById(id).map(transportadora -> {
            transportadora.setStatus(false);
            return repository.save(transportadora);
        });
    }
}
