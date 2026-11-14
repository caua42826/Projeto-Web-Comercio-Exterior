package br.com.projeto.comercio_exteriror.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.bootstrap")
public class BootstrapProperties {
    private ContaInicial user = new ContaInicial();
    private ContaInicial admin = new ContaInicial();

    @Getter
    @Setter
    public static class ContaInicial {
        private String username;
        private String nome;
        private String email;
        private String password;
    }
}
