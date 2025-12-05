package com.example.universidadeESN3.service;





import com.example.universidadeESN3.entity.Restaurante;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface IRestauranteService {
    Restaurante buscarPorId(Long id);
    List<Restaurante> buscarTodos();
    Restaurante salvar(Restaurante restaurante);
    ResponseEntity<?> atualizar(Restaurante restaurante);
    void excluir(Long id);
}