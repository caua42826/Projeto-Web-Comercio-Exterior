package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.dto.PerfilForm;
import br.com.projeto.comercio_exteriror.dto.PerfilResumo;
import br.com.projeto.comercio_exteriror.model.Perfil;
import br.com.projeto.comercio_exteriror.model.Permissao;
import br.com.projeto.comercio_exteriror.repository.PerfilRepository;
import br.com.projeto.comercio_exteriror.repository.PermissaoRepository;
import br.com.projeto.comercio_exteriror.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class PerfilService {
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;

    public PerfilService(PerfilRepository perfilRepository, PermissaoRepository permissaoRepository,
                         UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<PerfilResumo> listar() {
        return perfilRepository.findAllByOrderByCodigoAsc().stream().map(this::resumo).toList();
    }

    public List<Perfil> listarAtivos() {
        return perfilRepository.findAllByOrderByCodigoAsc().stream().filter(Perfil::isAtivo).toList();
    }

    public List<Permissao> listarPermissoes() {
        return permissaoRepository.findAllByOrderByCodigoAsc();
    }

    public PerfilForm buscarFormulario(Integer id) {
        Perfil perfil = buscarObrigatorio(id);
        PerfilForm form = new PerfilForm();
        form.setCodigo(perfil.getCodigo());
        form.setNome(perfil.getNome());
        form.setDescricao(perfil.getDescricao());
        form.setAtivo(perfil.isAtivo());
        form.setPermissoesIds(perfil.getPermissoes().stream().map(Permissao::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return form;
    }

    @Transactional
    public Perfil criar(PerfilForm form) {
        String codigo = normalizarCodigo(form.getCodigo());
        if (perfilRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RegraSegurancaException("codigo", "Esse código de perfil já existe.");
        }
        Perfil perfil = new Perfil();
        copiar(form, perfil, codigo);
        return perfilRepository.save(perfil);
    }

    @Transactional
    public Perfil atualizar(Integer id, PerfilForm form) {
        Perfil perfil = buscarObrigatorio(id);
        String codigo = normalizarCodigo(form.getCodigo());
        if (!perfil.getCodigo().equalsIgnoreCase(codigo)) {
            throw new RegraSegurancaException("codigo", "O código do perfil não pode ser alterado.");
        }
        if ("ADMIN".equalsIgnoreCase(codigo) && !form.isAtivo()) {
            throw new RegraSegurancaException("ativo", "O perfil ADMIN não pode ser desativado.");
        }
        copiar(form, perfil, codigo);
        return perfilRepository.save(perfil);
    }

    @Transactional
    public void excluir(Integer id) {
        Perfil perfil = buscarObrigatorio(id);
        if (Set.of("ADMIN", "USER").contains(perfil.getCodigo().toUpperCase(Locale.ROOT))) {
            throw new RegraSegurancaException("id", "Os perfis ADMIN e USER são protegidos e não podem ser excluídos.");
        }
        if (usuarioRepository.countByPerfis_Id(id) > 0) {
            throw new RegraSegurancaException("id", "Remova esse perfil dos usuários antes de excluí-lo.");
        }
        perfil.getPermissoes().clear();
        perfilRepository.save(perfil);
        perfilRepository.delete(perfil);
    }

    private void copiar(PerfilForm form, Perfil perfil, String codigo) {
        perfil.setCodigo(codigo);
        perfil.setNome(form.getNome().trim());
        perfil.setDescricao(limpar(form.getDescricao()));
        perfil.setAtivo(form.isAtivo());
        perfil.setPermissoes(carregarPermissoes(form.getPermissoesIds()));
    }

    private Set<Permissao> carregarPermissoes(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        List<Permissao> permissoes = permissaoRepository.findAllById(ids);
        if (permissoes.size() != ids.size()) {
            throw new RegraSegurancaException("permissoesIds", "Uma ou mais permissões não foram encontradas.");
        }
        return new LinkedHashSet<>(permissoes);
    }

    private PerfilResumo resumo(Perfil perfil) {
        return new PerfilResumo(perfil.getId(), perfil.getCodigo(), perfil.getNome(), perfil.getDescricao(),
                perfil.isAtivo(), perfil.getPermissoes().stream().map(Permissao::getCodigo).sorted().toList());
    }

    private Perfil buscarObrigatorio(Integer id) {
        return perfilRepository.findById(id)
                .orElseThrow(() -> new RegraSegurancaException("id", "Perfil não encontrado."));
    }

    private String normalizarCodigo(String valor) {
        if (valor == null || valor.isBlank()) throw new RegraSegurancaException("codigo", "Informe o código.");
        String codigo = valor.trim().toUpperCase(Locale.ROOT);
        if (!codigo.matches("[A-Z][A-Z0-9_]{2,59}")) {
            throw new RegraSegurancaException("codigo", "Use entre 3 e 60 letras maiúsculas, números ou sublinhado.");
        }
        return codigo;
    }

    private String limpar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
