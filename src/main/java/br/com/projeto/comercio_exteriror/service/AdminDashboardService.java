package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.repository.PerfilRepository;
import br.com.projeto.comercio_exteriror.repository.PermissaoRepository;
import br.com.projeto.comercio_exteriror.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardService {
    private final DashboardService dashboardService;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;

    public AdminDashboardService(DashboardService dashboardService, UsuarioRepository usuarioRepository,
                                 PerfilRepository perfilRepository, PermissaoRepository permissaoRepository) {
        this.dashboardService = dashboardService;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
    }

    public Resumo obterResumo() {
        return new Resumo(usuarioRepository.count(), usuarioRepository.countByAtivoTrue(),
                perfilRepository.count(), permissaoRepository.count(), dashboardService.obterContagens());
    }

    public record Resumo(long usuariosTotal, long usuariosAtivos, long perfis,
                         long permissoes, DashboardService.Contagens cadastros) { }
}
