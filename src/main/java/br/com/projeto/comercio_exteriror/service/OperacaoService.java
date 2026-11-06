package br.com.projeto.comercio_exteriror.service;

import br.com.projeto.comercio_exteriror.model.operacao.Operacao;
import br.com.projeto.comercio_exteriror.model.operacao.ItemOperacao;
import br.com.projeto.comercio_exteriror.model.Documento;
import br.com.projeto.comercio_exteriror.model.custo.CustoLogistico;
import br.com.projeto.comercio_exteriror.repository.OperacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OperacaoService {
    private final OperacaoRepository repository;

    public OperacaoService(OperacaoRepository repository) { this.repository = repository; }
    public List<Operacao> listarTodas() { return repository.findAll(); }
    public Optional<Operacao> buscarPorId(Integer id) { return repository.findById(id); }

    @Transactional
    public Operacao salvar(Operacao operacao) { return repository.save(operacao); }

    @Transactional
    public Optional<Operacao> adicionarItem(Integer idOperacao, ItemOperacao item) {
        return repository.findById(idOperacao).map(operacao -> {
            item.setOperacao(operacao);
            operacao.getItens().add(item);
            return repository.save(operacao);
        });
    }

    @Transactional
    public Optional<Operacao> removerItem(Integer idOperacao, Integer idItem) {
        return repository.findById(idOperacao).map(operacao -> {
            operacao.getItens().removeIf(item -> item.getId().equals(idItem));
            return repository.save(operacao);
        });
    }

    @Transactional
    public Optional<Operacao> adicionarDocumento(Integer idOperacao, Documento documento) {
        return repository.findById(idOperacao).map(operacao -> {
            documento.setOperacao(operacao);
            operacao.getDocumentos().add(documento);
            return repository.save(operacao);
        });
    }

    @Transactional
    public Optional<Operacao> removerDocumento(Integer idOperacao, Integer idDocumento) {
        return repository.findById(idOperacao).map(operacao -> {
            operacao.getDocumentos().removeIf(documento -> documento.getId().equals(idDocumento));
            return repository.save(operacao);
        });
    }

    @Transactional
    public Optional<Operacao> salvarCustoLogistico(Integer idOperacao, CustoLogistico custo) {
        return repository.findById(idOperacao).map(operacao -> {
            CustoLogistico destino = operacao.getCustoLogistico();
            if (destino == null) {
                destino = custo;
                destino.setOperacao(operacao);
                operacao.setCustoLogistico(destino);
            } else {
                destino.setMesAno(custo.getMesAno());
                destino.setCustoPortuario(custo.getCustoPortuario());
                destino.setCustoArmazenagem(custo.getCustoArmazenagem());
            }
            return repository.save(operacao);
        });
    }
}
