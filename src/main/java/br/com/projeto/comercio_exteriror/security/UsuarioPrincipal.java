package br.com.projeto.comercio_exteriror.security;

import br.com.projeto.comercio_exteriror.model.Perfil;
import br.com.projeto.comercio_exteriror.model.Permissao;
import br.com.projeto.comercio_exteriror.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.LinkedHashSet;
import java.util.Set;

public class UsuarioPrincipal implements UserDetails {
    private final Integer id;
    private final String username;
    private final String password;
    private final boolean ativo;
    private final boolean deveAlterarSenha;
    private final Set<GrantedAuthority> authorities;

    public UsuarioPrincipal(Usuario usuario) {
        id = usuario.getId();
        username = usuario.getUsername();
        password = usuario.getSenhaHash();
        ativo = usuario.isAtivo();
        deveAlterarSenha = usuario.isDeveAlterarSenha();
        authorities = new LinkedHashSet<>();

        for (Perfil perfil : usuario.getPerfis()) {
            if (!perfil.isAtivo()) continue;
            authorities.add(new SimpleGrantedAuthority("ROLE_" + perfil.getCodigo()));
            for (Permissao permissao : perfil.getPermissoes()) {
                if (permissao.isAtivo()) {
                    authorities.add(new SimpleGrantedAuthority(permissao.getCodigo()));
                }
            }
        }
    }

    public Integer getId() { return id; }

    public boolean isDeveAlterarSenha() { return deveAlterarSenha; }

    @Override public Set<GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return ativo; }
}
