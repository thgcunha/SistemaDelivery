package com.example.universidadeESN3.service;

import com.example.universidadeESN3.dto.CriarPedidoRequest;


import com.example.universidadeESN3.entity.Pedido;
import com.example.universidadeESN3.entity.StatusPedido;
import java.util.List;

public interface IPedidoService {
    Pedido buscarPorId(Long id);
    List<Pedido> buscarTodos();
    Pedido criarPedido(CriarPedidoRequest request);
    void atualizarStatus(Long id, StatusPedido status);
    void cancelarPedido(Long id);
}
