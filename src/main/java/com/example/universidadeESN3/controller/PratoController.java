package com.example.universidadeESN3.controller;

import com.example.delivery.entity.Prato;
import com.example.delivery.service.PratoService;
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
@RequestMapping("/prato")
@Slf4j
public class PratoController {

    @Autowired
    private PratoService pratoService;

    @GetMapping
    public ResponseEntity<List<Prato>> buscarTodos() {
        return ResponseEntity.ok(pratoService.buscarTodos());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Prato> buscarPorId(@PathVariable Long id){
        Prato prato = pratoService.buscarPorId(id);
        return ResponseEntity.ok(prato);
    }

    @GetMapping(path = "/nome/{nome}")
    public ResponseEntity<List<Prato>> buscarPorNome(@PathVariable String nome){
        List<Prato> response = pratoService.buscarPorNome(nome);
        if (response != null && !response.isEmpty()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping(path = "/categoria/{categoria}")
    public ResponseEntity<List<Prato>> buscarPorCategoria(@PathVariable String categoria){
        List<Prato> response = pratoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/restaurante/{restauranteId}")
    public ResponseEntity<List<Prato>> buscarPorRestaurante(@PathVariable Long restauranteId){
        List<Prato> response = pratoService.buscarPorRestaurante(restauranteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/disponiveis")
    public ResponseEntity<List<Prato>> buscarDisponiveis(){
        return ResponseEntity.ok(pratoService.buscarDisponiveis());
    }

    @GetMapping(path = "/page")
    public ResponseEntity<Page<Prato>> listarPratosPaginado(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Prato> pratos = pratoService.listarPaginado(pageable);
        return ResponseEntity.ok(pratos);
    }

    @PostMapping
    public ResponseEntity<Prato> salvar(@RequestBody Prato prato){
        log.info("salvar() - prato:{}", prato);
        return ResponseEntity.ok(pratoService.salvar(prato));
    }

    @PutMapping()
    public ResponseEntity<?> atualizar(@RequestBody Prato prato){
        return pratoService.atualizar(prato);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Prato response = pratoService.buscarPorId(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        pratoService.excluir(id);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/inactive/{id}")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        Prato response = pratoService.buscarPorId(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        pratoService.desativar(response);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<?> alterarDisponibilidade(@PathVariable Long id, @RequestParam Boolean disponivel) {
        pratoService.alterarDisponibilidade(id, disponivel);
        return ResponseEntity.ok(null);
    }
}
