package com.example.universidadeESN3.service;

import com.example.delivery.entity.Restaurante;
import com.example.delivery.exception.RestauranteNotFoundException;
import com.example.delivery.repository.RestauranteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class RestauranteService implements IRestauranteService {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Override
    public Restaurante buscarPorId(Long id) {
        try {
            return restauranteRepository.findById(id)
                    .orElseThrow(() -> new RestauranteNotFoundException(id));
        } catch (Exception e) {
            log.error("buscarPorId() - ERRO Inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar o restaurante por ID", e);
        }
    }

    @Override
    public List<Restaurante> buscarTodos() {
        return restauranteRepository.findAll();
    }

    @Override
    public Restaurante salvar(Restaurante restaurante) {
        log.info("salvar() - restaurante: {}", restaurante);
        return restauranteRepository.save(restaurante);
    }

    @Override
    public ResponseEntity<?> atualizar(Restaurante restaurante) {
        log.info("atualizar() - restaurante: {}", restaurante);
        Restaurante original = buscarPorId(restaurante.getId());
        if (original == null) {
            return ResponseEntity.notFound().build();
        }

        original.setNome(restaurante.getNome());
        original.setEndereco(restaurante.getEndereco());
        original.setTelefone(restaurante.getTelefone());
        original.setEspecialidade(restaurante.getEspecialidade());

        restauranteRepository.save(original);
        return ResponseEntity.ok(null);
    }

    @Override
    public void excluir(Long id) {
        restauranteRepository.deleteById(id);
    }

    public void desativar(Restaurante restaurante) {
        restaurante.setAtivo(Boolean.FALSE);
        restauranteRepository.save(restaurante);
    }

    public List<Restaurante> buscarPorNome(String nome) {
        return restauranteRepository.findByNomeStartingWithIgnoreCase(nome);
    }

    public List<Restaurante> buscarPorEspecialidade(String especialidade) {
        return restauranteRepository.findByEspecialidadeContainingIgnoreCase(especialidade);
    }

    public Page<Restaurante> listarPaginado(Pageable pageable) {
        return restauranteRepository.findAll(pageable);
    }
}