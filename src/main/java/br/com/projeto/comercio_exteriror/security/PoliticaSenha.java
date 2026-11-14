package br.com.projeto.comercio_exteriror.security;

import java.nio.charset.StandardCharsets;

public final class PoliticaSenha {
    private PoliticaSenha() { }

    public static boolean valida(String senha) {
        return senha != null && senha.length() >= 10
                && senha.getBytes(StandardCharsets.UTF_8).length <= 72
                && senha.matches("(?s).*[A-Z].*")
                && senha.matches("(?s).*[a-z].*")
                && senha.matches("(?s).*\\d.*")
                && senha.matches("(?s).*[^A-Za-z0-9].*");
    }
}
