package com.example.universidadeESN3.service;



import com.example.delivery.entity.Prato;
import com.example.delivery.exception.PratoNotFoundException;
import com.example.delivery.repository.PratoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class PratoService implements IPratoService {

    @Autowired
    private PratoRepository pratoRepository;

    @Override
    public Prato buscarPorId(Long id) {
        try {
            return pratoRepository.findById(id)
                    .orElseThrow(() -> new PratoNotFoundException(id));
        } catch (Exception e) {
            log.error("buscarPorId() - ERRO Inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar o prato por ID", e);
        }
    }

    @Override
    public List<Prato> buscarTodos() {
        return pratoRepository.findAll();
    }

    @Override
    public Prato salvar(Prato prato) {
        log.info("salvar() - prato: {}", prato);
        return pratoRepository.save(prato);
    }

    @Override
    public ResponseEntity<?> atualizar(Prato prato) {
        log.info("atualizar() - prato: {}", prato);
        Prato original = buscarPorId(prato.getId());
        if (original == null) {
            return ResponseEntity.notFound().build();
        }

        original.setNome(prato.getNome());
        original.setDescricao(prato.getDescricao());
        original.setPreco(prato.getPreco());
        original.setCategoria(prato.getCategoria());
        original.setTempoPreparoMinutos(prato.getTempoPreparoMinutos());
        original.setDisponivel(prato.getDisponivel());

        pratoRepository.save(original);
        return ResponseEntity.ok(null);
    }

    @Override
    public void excluir(Long id) {
        pratoRepository.deleteById(id);
    }

    public void desativar(Prato prato) {
        prato.setAtivo(Boolean.FALSE);
        pratoRepository.save(prato);
    }

    public List<Prato> buscarPorNome(String nome) {
        return pratoRepository.findByNomeStartingWithIgnoreCase(nome);
    }

    public List<Prato> buscarPorCategoria(String categoria) {
        return pratoRepository.findByCategoriaContainingIgnoreCase(categoria);
    }

    public List<Prato> buscarPorRestaurante(Long restauranteId) {
        return pratoRepository.findByRestauranteId(restauranteId);
    }

    public List<Prato> buscarDisponiveis() {
        return pratoRepository.findByDisponivelTrue();
    }

    public void alterarDisponibilidade(Long id, Boolean disponivel) {
        Prato prato = buscarPorId(id);
        prato.setDisponivel(disponivel);
        pratoRepository.save(prato);
    }

    public Page<Prato> listarPaginado(Pageable pageable) {
        return pratoRepository.findAll(pageable);
    }
}
