package com.example.universidadeESN3.exception;



public class PedidoNotFoundException extends RuntimeException {
    public PedidoNotFoundException(Long id) {
        super("Pedido não encontrado com o ID: " + id);
    }
}