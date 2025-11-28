package com.example.universidadeESN3.controller;



import com.example.delivery.entity.Pedido;
import com.example.delivery.entity.StatusPedido;
import com.example.delivery.service.PedidoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedido")
@Slf4j
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<Pedido>> buscarTodos() {
        return ResponseEntity.ok(pedidoService.buscarTodos());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id){
        Pedido pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping(path = "/status/{status}")
    public ResponseEntity<List<Pedido>> buscarPorStatus(@PathVariable StatusPedido status){
        return ResponseEntity.ok(pedidoService.buscarPorStatus(status));
    }

    @GetMapping(path = "/cliente/{nomeCliente}")
    public ResponseEntity<List<Pedido>> buscarPorCliente(@PathVariable String nomeCliente){
        return ResponseEntity.ok(pedidoService.buscarPorCliente(nomeCliente));
    }

    @GetMapping(path = "/page")
    public ResponseEntity<Page<Pedido>> listarPedidosPaginado(
            @PageableDefault(size = 10, sort = "dataHoraPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Pedido> pedidos = pedidoService.listarPaginado(pageable);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido){
        log.info("criarPedido() - pedido:{}", pedido);
        return ResponseEntity.ok(pedidoService.criarPedido(pedido));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestParam StatusPedido status){
        pedidoService.atualizarStatus(id, status);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(null);
    }
}
