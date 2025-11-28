package com.example.universidadeESN3.service;

i
import com.example.universidadeESN3.entity.Prato;

import java.util.List;


import com.example.delivery.entity.Prato;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface IPratoService {
    Prato buscarPorId(Long id);
    List<Prato> buscarTodos();
    Prato salvar(Prato prato);
    ResponseEntity<?> atualizar(Prato prato);
    void excluir(Long id);
}
