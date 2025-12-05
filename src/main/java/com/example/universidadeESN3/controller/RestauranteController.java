package com.example.universidadeESN3.controller;

import com.example.universidadeESN3.entity.Restaurante;
import com.example.universidadeESN3.service.RestauranteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/restaurante")
@Slf4j
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;

    @GetMapping
    public ResponseEntity<List<Restaurante>> buscarTodos() {
        return ResponseEntity.ok(restauranteService.buscarTodos());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Restaurante> buscarPorId(@PathVariable Long id){
        Restaurante restaurante = restauranteService.buscarPorId(id);
        return ResponseEntity.ok(restaurante);
    }

    @GetMapping(path = "/nome/{nome}")
    public ResponseEntity<List<Restaurante>> buscarPorNome(@PathVariable String nome){
        List<Restaurante> response = restauranteService.buscarPorNome(nome);
        if (response != null && !response.isEmpty()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping(path = "/especialidade/{especialidade}")
    public ResponseEntity<List<Restaurante>> buscarPorEspecialidade(@PathVariable String especialidade){
        List<Restaurante> response = restauranteService.buscarPorEspecialidade(especialidade);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/page")
    public ResponseEntity<Page<Restaurante>> listarRestaurantesPaginado(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Restaurante> restaurantes = restauranteService.listarPaginado(pageable);
        return ResponseEntity.ok(restaurantes);
    }

    @PostMapping
    public ResponseEntity<Restaurante> salvar(@RequestBody Restaurante restaurante){
        log.info("salvar() - restaurante:{}", restaurante);
        return ResponseEntity.ok(restauranteService.salvar(restaurante));
    }

    @PutMapping()
    public ResponseEntity<?> atualizar(@RequestBody Restaurante restaurante){
        return restauranteService.atualizar(restaurante);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Restaurante response = restauranteService.buscarPorId(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        restauranteService.excluir(id);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/inactive/{id}")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        Restaurante response = restauranteService.buscarPorId(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        restauranteService.desativar(response);
        return ResponseEntity.ok(null);
    }
}
