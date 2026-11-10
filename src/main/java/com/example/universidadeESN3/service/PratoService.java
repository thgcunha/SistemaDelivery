package com.example.universidadeESN3.service;



import com.example.universidadeESN3.entity.Prato;
import com.example.universidadeESN3.exception.PratoNotFoundException;
import com.example.universidadeESN3.exception.RegraNegocioException;
import com.example.universidadeESN3.exception.RestauranteNotFoundException;
import com.example.universidadeESN3.repository.PratoRepository;
import com.example.universidadeESN3.repository.RestauranteRepository;
import com.example.universidadeESN3.entity.Restaurante;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class PratoService implements IPratoService {

    @Autowired
    private PratoRepository pratoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Override
    public Prato buscarPorId(Long id) {
        return pratoRepository.findById(id)
                .orElseThrow(() -> new PratoNotFoundException(id));
    }

    @Override
    public List<Prato> buscarTodos() {
        return pratoRepository.findAll();
    }

    @Override
    @Transactional
    public Prato salvar(Prato prato) {
        if (prato.getId() != null) {
            throw new IllegalArgumentException("Não informe um ID ao criar um prato.");
        }
        prato.setRestaurante(buscarRestauranteAtivo(prato));
        log.info("salvar() - prato: {}", prato);
        return pratoRepository.save(prato);
    }

    @Override
    @Transactional
    public ResponseEntity<?> atualizar(Prato prato) {
        if (prato.getId() == null) {
            throw new IllegalArgumentException("Informe o ID do prato para atualização.");
        }
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
        original.setRestaurante(buscarRestauranteAtivo(prato));

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

    private Restaurante buscarRestauranteAtivo(Prato prato) {
        Long restauranteId = prato.getRestaurante().getId();
        if (restauranteId == null) {
            throw new IllegalArgumentException("Informe o ID do restaurante do prato.");
        }
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RestauranteNotFoundException(restauranteId));
        if (!Boolean.TRUE.equals(restaurante.getAtivo())) {
            throw new RegraNegocioException("O restaurante do prato está inativo.");
        }
        return restaurante;
    }
}
