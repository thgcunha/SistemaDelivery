package com.example.universidadeESN3.service;


import com.example.universidadeESN3.entity.Prato;

import java.util.List;
import org.springframework.http.ResponseEntity;


public interface IPratoService {
    Prato buscarPorId(Long id);
    List<Prato> buscarTodos();
    Prato salvar(Prato prato);
    ResponseEntity<?> atualizar(Prato prato);
    void excluir(Long id);
}
