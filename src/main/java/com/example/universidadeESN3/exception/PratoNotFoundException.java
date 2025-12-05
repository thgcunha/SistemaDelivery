package com.example.universidadeESN3.exception;



public class PratoNotFoundException extends RuntimeException {
    public PratoNotFoundException(Long id) {
        super("Prato não encontrado com o ID: " + id);
    }
}