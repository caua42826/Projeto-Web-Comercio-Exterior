package br.com.projeto.comercio_exteriror.service;

public class RegraSegurancaException extends RuntimeException {
    private final String campo;

    public RegraSegurancaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
}
