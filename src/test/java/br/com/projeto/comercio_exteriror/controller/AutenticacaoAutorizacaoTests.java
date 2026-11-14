package br.com.projeto.comercio_exteriror.controller;

import br.com.projeto.comercio_exteriror.model.Usuario;
import br.com.projeto.comercio_exteriror.repository.PerfilRepository;
import br.com.projeto.comercio_exteriror.repository.PermissaoRepository;
import br.com.projeto.comercio_exteriror.repository.UsuarioRepository;
import br.com.projeto.comercio_exteriror.service.SecurityDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:security_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class AutenticacaoAutorizacaoTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private PermissaoRepository permissaoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SecurityDataSeeder dataSeeder;

    @Test
    void bloqueiaAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/empresas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login"));
    }

    @Test
    void loginValidoUsaUsuarioDoBancoEExigeTrocaDaSenhaInicial() throws Exception {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase("usuario").orElseThrow();
        assertThat(usuario.getSenhaHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Usuario@123", usuario.getSenhaHash())).isTrue();

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "usuario")
                        .param("password", "Usuario@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/conta/senha?obrigatoria"));
    }

    @Test
    void loginInvalidoRedirecionaComMensagemGenerica() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "usuario")
                        .param("password", "senha-errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?erro"));
    }

    @Test
    @WithMockUser(username = "visualizador", authorities = {"DADOS_CONSULTAR"})
    void usuarioPadraoPodeConsultarMasNaoPodeAlterarNemAbrirFormularioDeEdicao() throws Exception {
        mockMvc.perform(get("/empresas"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Nova empresa"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Editar"))));
        mockMvc.perform(get("/empresas/nova")).andExpect(status().isForbidden());
        mockMvc.perform(post("/empresas").with(csrf()).param("nome", "Bloqueio"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void administradorAcessaPainelEAdministracaoDeUsuarios() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isOk()).andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/usuarios")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/perfis")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/permissoes")).andExpect(status().isOk());
    }

    @Test
    void logoutExigeCsrfEEncerraSessao() throws Exception {
        MvcResult login = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin")
                        .param("password", "Admin@123456"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?logout"));
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @Transactional
    void provisionamentoEhIdempotenteEIncluiPerfisEPermissoes() throws Exception {
        long usuarios = usuarioRepository.count();
        long perfis = perfilRepository.count();
        long permissoes = permissaoRepository.count();
        ApplicationArguments argumentos = new DefaultApplicationArguments(new String[0]);

        dataSeeder.run(argumentos);
        dataSeeder.run(argumentos);

        assertThat(usuarioRepository.count()).isEqualTo(usuarios);
        assertThat(perfilRepository.count()).isEqualTo(perfis);
        assertThat(permissaoRepository.count()).isEqualTo(permissoes);
        assertThat(usuarioRepository.findByUsernameIgnoreCase("admin").orElseThrow().getPerfis())
                .anyMatch(perfil -> perfil.getCodigo().equals("ADMIN"));
    }

    @Test
    @Transactional
    void contaInativaNaoAutentica() throws Exception {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase("usuario").orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "usuario")
                        .param("password", "Usuario@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?bloqueado"));
    }
}
