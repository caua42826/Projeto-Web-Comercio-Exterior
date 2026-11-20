package br.com.projeto.comercio_exteriror.config;

import br.com.projeto.comercio_exteriror.security.ForcePasswordChangeFilter;
import br.com.projeto.comercio_exteriror.security.LoginFailureHandler;
import br.com.projeto.comercio_exteriror.security.LoginSuccessHandler;
import br.com.projeto.comercio_exteriror.security.PermissoesSistema;
import br.com.projeto.comercio_exteriror.security.UsuarioDetailsService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(BootstrapProperties.class)
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationProvider authenticationProvider(UsuarioDetailsService usuarioDetailsService,
                                                  PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationProvider authenticationProvider,
                                            LoginSuccessHandler successHandler,
                                            LoginFailureHandler failureHandler,
                                            ForcePasswordChangeFilter forcePasswordChangeFilter,
                                            SessionRegistry sessionRegistry) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/error", "/acesso-negado", "/css/**", "/js/**", "/images/**", "/videos/**", "/favicon.ico").permitAll()
                        .requestMatchers("/admin/**", "/h2-console/**").hasRole("ADMIN")
                        .requestMatchers("/conta/**").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/empresas/nova", "/empresas/*/editar",
                                "/parceiros/novo", "/parceiros/*/editar",
                                "/produtos/novo", "/produtos/*/editar",
                                "/transportadoras/nova", "/transportadoras/*/editar",
                                "/taxas-cambio/nova", "/taxas-cambio/*/editar",
                                "/operacoes/nova", "/operacoes/*/editar")
                        .hasAnyAuthority(PermissoesSistema.DADOS_GERENCIAR, "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyAuthority(PermissoesSistema.DADOS_CONSULTAR, "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/**").hasAnyAuthority(PermissoesSistema.DADOS_GERENCIAR, "ROLE_ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expirada")
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expirada")
                        .sessionRegistry(sessionRegistry))
                .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/acesso-negado"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterAfter(forcePasswordChangeFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
