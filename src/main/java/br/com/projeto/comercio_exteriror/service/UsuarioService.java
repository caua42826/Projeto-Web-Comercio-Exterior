package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.dto.UsuarioForm;
import br.com.projeto.comercio_exteriror.dto.UsuarioResumo;
import br.com.projeto.comercio_exteriror.model.Perfil;
import br.com.projeto.comercio_exteriror.model.Usuario;
import br.com.projeto.comercio_exteriror.repository.PerfilRepository;
import br.com.projeto.comercio_exteriror.repository.UsuarioRepository;
import br.com.projeto.comercio_exteriror.security.PoliticaSenha;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResumo> listar() {
        return usuarioRepository.findAllByOrderByNomeCompletoAsc().stream().map(this::resumo).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioForm buscarFormulario(Integer id) {
        Usuario usuario = buscarObrigatorio(id);
        UsuarioForm form = new UsuarioForm();
        form.setNomeCompleto(usuario.getNomeCompleto());
        form.setUsername(usuario.getUsername());
        form.setEmail(usuario.getEmail());
        form.setAtivo(usuario.isAtivo());
        form.setPerfisIds(usuario.getPerfis().stream().map(Perfil::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return form;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResumo buscarResumo(Integer id) {
        return resumo(buscarObrigatorio(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Usuario criar(UsuarioForm form) {
        String username = normalizarUsername(form.getUsername());
        String email = normalizarEmail(form.getEmail());
        verificarIdentidade(form, null, username, email);
        validarSenha(form.getSenha());

        Usuario usuario = new Usuario();
        copiarCampos(form, usuario, username, email);
        usuario.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        usuario.setDeveAlterarSenha(true);
        usuario.setPerfis(carregarPerfis(form.getPerfisIds()));
        return usuarioRepository.save(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Usuario atualizar(Integer id, UsuarioForm form) {
        Usuario usuario = buscarObrigatorio(id);
        String username = normalizarUsername(form.getUsername());
        String email = normalizarEmail(form.getEmail());
        verificarIdentidade(form, id, username, email);

        if (form.getSenha() != null && !form.getSenha().isBlank()) {
            validarSenha(form.getSenha());
            usuario.setSenhaHash(passwordEncoder.encode(form.getSenha()));
            usuario.setDeveAlterarSenha(true);
        }
        Set<Perfil> novosPerfis = carregarPerfis(form.getPerfisIds());
        protegerUltimoAdministrador(usuario, form.isAtivo(), novosPerfis);
        copiarCampos(form, usuario, username, email);
        usuario.setPerfis(novosPerfis);
        return usuarioRepository.save(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UsuarioResumo alterarStatus(Integer id, boolean ativo) {
        Usuario usuario = buscarObrigatorio(id);
        protegerUltimoAdministrador(usuario, ativo, usuario.getPerfis());
        usuario.setAtivo(ativo);
        return resumo(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void redefinirSenha(Integer id, String senha) {
        validarSenha(senha);
        Usuario usuario = buscarObrigatorio(id);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setDeveAlterarSenha(true);
        usuarioRepository.save(usuario);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void alterarMinhaSenha(String username, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RegraSegurancaException("senhaAtual", "Não foi possível validar a conta."));
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraSegurancaException("senhaAtual", "A senha atual está incorreta.");
        }
        validarSenha(novaSenha);
        if (passwordEncoder.matches(novaSenha, usuario.getSenhaHash())) {
            throw new RegraSegurancaException("novaSenha", "Escolha uma senha diferente da atual.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setDeveAlterarSenha(false);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarObrigatorio(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraSegurancaException("id", "Usuário não encontrado."));
    }

    private UsuarioResumo resumo(Usuario usuario) {
        return new UsuarioResumo(usuario.getId(), usuario.getNomeCompleto(), usuario.getUsername(),
                usuario.getEmail(), usuario.isAtivo(), usuario.isDeveAlterarSenha(),
                usuario.getPerfis().stream().map(Perfil::getNome).sorted().toList(), usuario.getCriadoEm());
    }

    private void verificarIdentidade(UsuarioForm form, Integer id, String username, String email) {
        usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(usuario -> id == null || !usuario.getId().equals(id))
                .ifPresent(usuario -> { throw new RegraSegurancaException("username", "Esse nome de usuário já está em uso."); });
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(usuario -> id == null || !usuario.getId().equals(id))
                .ifPresent(usuario -> { throw new RegraSegurancaException("email", "Esse e-mail já está em uso."); });
        if (form.getNomeCompleto() == null || form.getNomeCompleto().isBlank()) {
            throw new RegraSegurancaException("nomeCompleto", "Informe o nome completo.");
        }
    }

    private void copiarCampos(UsuarioForm form, Usuario usuario, String username, String email) {
        usuario.setNomeCompleto(form.getNomeCompleto().trim());
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setAtivo(form.isAtivo());
    }

    private Set<Perfil> carregarPerfis(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RegraSegurancaException("perfisIds", "Selecione ao menos um perfil.");
        }
        List<Perfil> perfis = perfilRepository.findAllById(ids);
        if (perfis.size() != ids.size() || perfis.stream().anyMatch(perfil -> !perfil.isAtivo())) {
            throw new RegraSegurancaException("perfisIds", "Um ou mais perfis selecionados não estão disponíveis.");
        }
        return new LinkedHashSet<>(perfis);
    }

    private void protegerUltimoAdministrador(Usuario usuarioAtual, boolean ativo, Set<Perfil> novosPerfis) {
        boolean eraAdminAtivo = usuarioAtual != null && usuarioAtual.isAtivo()
                && possuiPerfil(usuarioAtual.getPerfis(), "ADMIN");
        boolean continuaAdminAtivo = ativo && possuiPerfil(novosPerfis, "ADMIN");
        if (eraAdminAtivo && !continuaAdminAtivo
                && usuarioRepository.countByAtivoTrueAndPerfis_CodigoIgnoreCase("ADMIN") <= 1) {
            throw new RegraSegurancaException("perfisIds", "Não é possível remover ou desativar o último administrador ativo.");
        }
    }

    private boolean possuiPerfil(Set<Perfil> perfis, String codigo) {
        return perfis.stream().anyMatch(perfil -> perfil.isAtivo() && perfil.getCodigo().equalsIgnoreCase(codigo));
    }

    private String normalizarUsername(String valor) {
        if (valor == null || valor.isBlank()) throw new RegraSegurancaException("username", "Informe o nome de usuário.");
        String username = valor.trim().toLowerCase(Locale.ROOT);
        if (username.length() < 3 || username.length() > 60 || !username.matches("[a-z0-9._-]+")) {
            throw new RegraSegurancaException("username", "Use 3 a 60 letras, números, ponto, hífen ou sublinhado.");
        }
        return username;
    }

    private String normalizarEmail(String valor) {
        if (valor == null || valor.isBlank()) throw new RegraSegurancaException("email", "Informe o e-mail.");
        String email = valor.trim().toLowerCase(Locale.ROOT);
        if (email.length() > 180 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RegraSegurancaException("email", "Informe um endereço de e-mail válido.");
        }
        return email;
    }

    private void validarSenha(String senha) {
        if (!PoliticaSenha.valida(senha)) {
            throw new RegraSegurancaException("senha", "Use ao menos 10 caracteres, com maiúscula, minúscula, número e símbolo (até 72 bytes).");
        }
    }
}
