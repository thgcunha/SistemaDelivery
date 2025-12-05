package com.example.universidadeESN3.exception;


public class RestauranteNotFoundException extends RuntimeException {
    public RestauranteNotFoundException(Long id) {
        super("Restaurante não encontrado com o ID: " + id);
    }
}
