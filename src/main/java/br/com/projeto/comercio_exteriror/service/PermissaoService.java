package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.dto.PermissaoForm;
import br.com.projeto.comercio_exteriror.dto.PermissaoResumo;
import br.com.projeto.comercio_exteriror.model.Permissao;
import br.com.projeto.comercio_exteriror.repository.PermissaoRepository;
import br.com.projeto.comercio_exteriror.security.PermissoesSistema;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class PermissaoService {
    private static final Set<String> CODIGOS_SISTEMA = Set.of(
            PermissoesSistema.DADOS_CONSULTAR,
            PermissoesSistema.DADOS_GERENCIAR,
            PermissoesSistema.USUARIOS_GERENCIAR,
            PermissoesSistema.PERFIS_GERENCIAR,
            PermissoesSistema.PERMISSOES_GERENCIAR);

    private final PermissaoRepository permissaoRepository;

    public PermissaoService(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    public java.util.List<PermissaoResumo> listar() {
        return permissaoRepository.findAllByOrderByCodigoAsc().stream().map(this::resumo).toList();
    }

    public PermissaoForm buscarFormulario(Integer id) {
        Permissao permissao = buscarObrigatoria(id);
        PermissaoForm form = new PermissaoForm();
        form.setCodigo(permissao.getCodigo());
        form.setNome(permissao.getNome());
        form.setDescricao(permissao.getDescricao());
        form.setAtivo(permissao.isAtivo());
        return form;
    }

    @Transactional
    public Permissao criar(PermissaoForm form) {
        String codigo = normalizarCodigo(form.getCodigo());
        if (permissaoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RegraSegurancaException("codigo", "Esse código de permissão já existe.");
        }
        Permissao permissao = new Permissao();
        copiar(form, permissao, codigo);
        return permissaoRepository.save(permissao);
    }

    @Transactional
    public Permissao atualizar(Integer id, PermissaoForm form) {
        Permissao permissao = buscarObrigatoria(id);
        String codigo = normalizarCodigo(form.getCodigo());
        if (!permissao.getCodigo().equalsIgnoreCase(codigo)) {
            throw new RegraSegurancaException("codigo", "O código da permissão não pode ser alterado.");
        }
        copiar(form, permissao, codigo);
        return permissaoRepository.save(permissao);
    }

    @Transactional
    public void excluir(Integer id) {
        Permissao permissao = buscarObrigatoria(id);
        if (CODIGOS_SISTEMA.contains(permissao.getCodigo())) {
            throw new RegraSegurancaException("id", "Permissões internas do sistema não podem ser excluídas.");
        }
        if (permissaoRepository.existsByPerfis_Id(id)) {
            throw new RegraSegurancaException("id", "Remova essa permissão dos perfis antes de excluí-la.");
        }
        permissaoRepository.delete(permissao);
    }

    private void copiar(PermissaoForm form, Permissao permissao, String codigo) {
        permissao.setCodigo(codigo);
        permissao.setNome(form.getNome().trim());
        permissao.setDescricao(form.getDescricao() == null || form.getDescricao().isBlank() ? null : form.getDescricao().trim());
        permissao.setAtivo(form.isAtivo());
    }

    private PermissaoResumo resumo(Permissao permissao) {
        return new PermissaoResumo(permissao.getId(), permissao.getCodigo(), permissao.getNome(),
                permissao.getDescricao(), permissao.isAtivo(), CODIGOS_SISTEMA.contains(permissao.getCodigo()));
    }

    private Permissao buscarObrigatoria(Integer id) {
        return permissaoRepository.findById(id)
                .orElseThrow(() -> new RegraSegurancaException("id", "Permissão não encontrada."));
    }

    private String normalizarCodigo(String valor) {
        if (valor == null || valor.isBlank()) throw new RegraSegurancaException("codigo", "Informe o código.");
        String codigo = valor.trim().toUpperCase(Locale.ROOT);
        if (!codigo.matches("[A-Z][A-Z0-9_]{2,79}")) {
            throw new RegraSegurancaException("codigo", "Use letras maiúsculas, números ou sublinhado.");
        }
        return codigo;
    }
}
