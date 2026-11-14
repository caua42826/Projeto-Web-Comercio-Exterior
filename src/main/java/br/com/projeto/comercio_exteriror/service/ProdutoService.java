package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.Produto;
import br.com.projeto.comercio_exteriror.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) { this.repository = repository; }
    public List<Produto> listarTodos() { return repository.findAll(); }
    public Optional<Produto> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Produto salvar(Produto produto) { return repository.save(produto); }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Optional<Produto> desativar(Integer id) {
        return repository.findById(id).map(produto -> {
            produto.setStatus(false);
            return repository.save(produto);
        });
    }
}
