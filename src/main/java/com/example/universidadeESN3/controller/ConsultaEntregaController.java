package com.example.universidadeESN3.controller;

import com.example.universidadeESN3.service.ConsultaEntregaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConsultaEntregaController {
    private final ConsultaEntregaService consultas;

    public ConsultaEntregaController(ConsultaEntregaService consultas) {
        this.consultas = consultas;
    }

    @GetMapping("/cep/{cep}")
    public ConsultaEntregaService.Endereco consultarCep(@PathVariable String cep) {
        return consultas.consultarCep(cep);
    }

    @GetMapping("/entrega/estimativa")
    public ConsultaEntregaService.Estimativa estimar(
            @RequestParam Long restauranteId, @RequestParam String cep) {
        return consultas.estimar(restauranteId, cep);
    }
}
