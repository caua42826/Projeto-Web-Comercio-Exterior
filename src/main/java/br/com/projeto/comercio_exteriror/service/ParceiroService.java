package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.Parceiro;
import br.com.projeto.comercio_exteriror.repository.ParceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class ParceiroService {
    private final ParceiroRepository repository;

    public ParceiroService(ParceiroRepository repository) { this.repository = repository; }
    public List<Parceiro> listarTodos() { return repository.findAll(); }
    public Optional<Parceiro> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Parceiro salvar(Parceiro parceiro) { return repository.save(parceiro); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Optional<Parceiro> desativar(Integer id) {
        return repository.findById(id).map(parceiro -> {
            parceiro.setStatus(false);
            return repository.save(parceiro);
        });
    }
}
