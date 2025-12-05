package com.example.universidadeESN3.service;



import com.example.universidadeESN3.entity.Pedido;
import com.example.universidadeESN3.entity.StatusPedido;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface IPedidoService {
    Pedido buscarPorId(Long id);
    List<Pedido> buscarTodos();
    Pedido criarPedido(Pedido pedido);
    void atualizarStatus(Long id, StatusPedido status);
    void cancelarPedido(Long id);
}
