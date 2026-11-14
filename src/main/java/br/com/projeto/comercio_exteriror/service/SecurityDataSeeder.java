package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.config.BootstrapProperties;
import br.com.projeto.comercio_exteriror.model.Perfil;
import br.com.projeto.comercio_exteriror.model.Permissao;
import br.com.projeto.comercio_exteriror.model.Usuario;
import br.com.projeto.comercio_exteriror.repository.PerfilRepository;
import br.com.projeto.comercio_exteriror.repository.PermissaoRepository;
import br.com.projeto.comercio_exteriror.repository.UsuarioRepository;
import br.com.projeto.comercio_exteriror.security.PermissoesSistema;
import br.com.projeto.comercio_exteriror.security.PoliticaSenha;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class SecurityDataSeeder implements ApplicationRunner {
    private final BootstrapProperties properties;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityDataSeeder(BootstrapProperties properties,
                              UsuarioRepository usuarioRepository,
                              PerfilRepository perfilRepository,
                              PermissaoRepository permissaoRepository,
                              PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<Permissao> permissoes = criarPermissoesBasicas();
        Perfil admin = obterOuCriarPerfil("ADMIN", "Administrador",
                "Acesso administrativo completo.", permissoes);
        Perfil user = obterOuCriarPerfil("USER", "Usuário padrão",
                "Acesso de consulta aos dados.", Set.of(permissao(PermissoesSistema.DADOS_CONSULTAR)));

        provisionarUsuarioPadrao(user);
        provisionarAdministrador(admin);
    }

    private Set<Permissao> criarPermissoesBasicas() {
        Set<Permissao> permissoes = new LinkedHashSet<>();
        for (PermissoesSistema.Definicao definicao : PermissoesSistema.DEFINICOES) {
            Permissao permissao = permissaoRepository.findByCodigoIgnoreCase(definicao.codigo())
                    .orElseGet(() -> {
                        Permissao nova = new Permissao();
                        nova.setCodigo(definicao.codigo());
                        nova.setNome(definicao.nome());
                        nova.setDescricao(definicao.descricao());
                        nova.setAtivo(true);
                        return permissaoRepository.save(nova);
                    });
            permissoes.add(permissao);
        }
        return permissoes;
    }

    private Perfil obterOuCriarPerfil(String codigo, String nome, String descricao,
                                      Set<Permissao> permissoesIniciais) {
        return perfilRepository.findByCodigoIgnoreCase(codigo).orElseGet(() -> {
            Perfil perfil = new Perfil();
            perfil.setCodigo(codigo);
            perfil.setNome(nome);
            perfil.setDescricao(descricao);
            perfil.setAtivo(true);
            perfil.setPermissoes(new LinkedHashSet<>(permissoesIniciais));
            return perfilRepository.save(perfil);
        });
    }

    private Permissao permissao(String codigo) {
        return permissaoRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalStateException("Permissão inicial não foi criada."));
    }

    private void provisionarUsuarioPadrao(Perfil perfil) {
        BootstrapProperties.ContaInicial conta = properties.getUser();
        if (usuarioRepository.existsByUsernameIgnoreCase(limpar(conta.getUsername()))) return;
        validarContaInicial(conta, "APP_BOOTSTRAP_USER_USERNAME, APP_BOOTSTRAP_USER_EMAIL e APP_BOOTSTRAP_USER_PASSWORD");
        if (usuarioRepository.existsByEmailIgnoreCase(limpar(conta.getEmail()))) {
            throw new IllegalStateException("O e-mail configurado para o usuário inicial já está cadastrado.");
        }
        usuarioRepository.save(novoUsuario(conta, perfil));
    }

    private void provisionarAdministrador(Perfil perfil) {
        if (usuarioRepository.existsByAtivoTrueAndPerfis_CodigoIgnoreCase("ADMIN")) return;
        BootstrapProperties.ContaInicial conta = properties.getAdmin();
        validarContaInicial(conta, "APP_BOOTSTRAP_ADMIN_USERNAME, APP_BOOTSTRAP_ADMIN_EMAIL e APP_BOOTSTRAP_ADMIN_PASSWORD");
        if (usuarioRepository.existsByUsernameIgnoreCase(limpar(conta.getUsername()))) {
            throw new IllegalStateException("O username configurado para o administrador inicial já existe sem acesso ADMIN.");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(limpar(conta.getEmail()))) {
            throw new IllegalStateException("O e-mail configurado para o administrador inicial já está cadastrado.");
        }
        usuarioRepository.save(novoUsuario(conta, perfil));
    }

    private Usuario novoUsuario(BootstrapProperties.ContaInicial conta, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(limpar(conta.getNome()));
        usuario.setUsername(normalizarUsername(conta.getUsername()));
        usuario.setEmail(limpar(conta.getEmail()).toLowerCase(Locale.ROOT));
        usuario.setSenhaHash(passwordEncoder.encode(conta.getPassword()));
        usuario.setAtivo(true);
        usuario.setDeveAlterarSenha(true);
        usuario.setPerfis(new LinkedHashSet<>(Set.of(perfil)));
        return usuario;
    }

    private void validarContaInicial(BootstrapProperties.ContaInicial conta, String variaveis) {
        if (conta == null || vazio(conta.getUsername()) || vazio(conta.getNome())
                || vazio(conta.getEmail()) || vazio(conta.getPassword())) {
            throw new IllegalStateException("Configure as variáveis " + variaveis + " antes da primeira inicialização.");
        }
        if (!PoliticaSenha.valida(conta.getPassword())) {
            throw new IllegalStateException("A senha bootstrap precisa ter 10 a 72 bytes, com maiúscula, minúscula, número e símbolo.");
        }
        if (conta.getUsername().trim().length() < 3 || conta.getUsername().trim().length() > 60
                || !conta.getUsername().trim().matches("[A-Za-z0-9._-]+")) {
            throw new IllegalStateException("O username de uma conta bootstrap deve ter 3 a 60 letras, números, ponto, hífen ou sublinhado.");
        }
        if (conta.getNome().trim().length() > 150) {
            throw new IllegalStateException("O nome de uma conta bootstrap deve ter até 150 caracteres.");
        }
        if (!conta.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException("O e-mail configurado para uma conta inicial é inválido.");
        }
    }

    private String normalizarUsername(String username) {
        return limpar(username).toLowerCase(Locale.ROOT);
    }

    private String limpar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
