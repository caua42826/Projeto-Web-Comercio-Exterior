package br.com.projeto.comercio_exteriror.security;

import br.com.projeto.comercio_exteriror.model.Usuario;
import br.com.projeto.comercio_exteriror.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.carregarParaAutenticacao(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        return new UsuarioPrincipal(usuario);
    }
}
