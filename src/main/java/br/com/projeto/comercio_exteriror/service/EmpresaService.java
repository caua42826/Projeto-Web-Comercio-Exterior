package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.Empresa;
import br.com.projeto.comercio_exteriror.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('DADOS_CONSULTAR') or hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    public Optional<Empresa> buscarPorId(Integer id) {
        return empresaRepository.findById(id);
    }

    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Empresa salvar(Empresa empresa) {
        return empresaRepository.save(empresa);
    }

    /** Desativa sem apagar o registro, preservando operações históricas. */
    @Transactional
    @PreAuthorize("hasAuthority('DADOS_GERENCIAR') or hasRole('ADMIN')")
    public Optional<Empresa> desativar(Integer id) {
        return empresaRepository.findById(id).map(empresa -> {
            empresa.setStatus(false);
            return empresaRepository.save(empresa);
        });
    }
}
