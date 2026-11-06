package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.Parceiro;
import br.com.projeto.comercio_exteriror.repository.ParceiroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ParceiroService {
    private final ParceiroRepository repository;

    public ParceiroService(ParceiroRepository repository) { this.repository = repository; }
    public List<Parceiro> listarTodos() { return repository.findAll(); }
    public Optional<Parceiro> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    public Parceiro salvar(Parceiro parceiro) { return repository.save(parceiro); }

    @Transactional
    public Optional<Parceiro> desativar(Integer id) {
        return repository.findById(id).map(parceiro -> {
            parceiro.setStatus(false);
            return repository.save(parceiro);
        });
    }
}
