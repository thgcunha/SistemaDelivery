package com.example.universidadeESN3.service;

import com.example.universidadeESN3.entity.Restaurante;
import com.example.universidadeESN3.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultaEntregaServiceTest {
    @Test
    void consultaEnderecoERotaComServicosSimulados() throws Exception {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/ws/01001000/json/", troca -> responder(troca,
                "{\"cep\":\"01001-000\",\"logradouro\":\"Praça da Sé\",\"bairro\":\"Sé\",\"localidade\":\"São Paulo\",\"uf\":\"SP\"}"));
        servidor.createContext("/api/cep/v2/01001000", troca -> responder(troca,
                "{\"location\":{\"coordinates\":{\"latitude\":\"-23.5504\",\"longitude\":\"-46.6331\"}}}"));
        servidor.createContext("/route/v1/driving/", troca -> responder(troca,
                "{\"routes\":[{\"distance\":3250,\"duration\":900}]}"));
        servidor.start();
        try {
            Restaurante restaurante = new Restaurante();
            restaurante.setId(1L);
            restaurante.setNome("Teste");
            restaurante.setLatitude(-23.5600);
            restaurante.setLongitude(-46.6500);
            RestauranteRepository repository = mock(RestauranteRepository.class);
            when(repository.findById(1L)).thenReturn(Optional.of(restaurante));
            ConsultaEntregaService service = new ConsultaEntregaService(new ObjectMapper(), repository);
            String origem = "http://127.0.0.1:" + servidor.getAddress().getPort();
            ReflectionTestUtils.setField(service, "viaCepUrl", origem);
            ReflectionTestUtils.setField(service, "brasilApiUrl", origem);
            ReflectionTestUtils.setField(service, "osrmUrl", origem);

            assertThat(service.consultarCep("01001-000").logradouro()).isEqualTo("Praça da Sé");
            var estimativa = service.estimar(1L, "01001000");
            assertThat(estimativa.distanciaKm()).isEqualTo(3.3);
            assertThat(estimativa.duracaoMinutos()).isEqualTo(15);
            assertThat(estimativa.frete()).isEqualByComparingTo("10.75");
        } finally {
            servidor.stop(0);
        }
    }

    private static void responder(com.sun.net.httpserver.HttpExchange troca, String json) throws IOException {
        byte[] resposta = json.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().add("Content-Type", "application/json");
        troca.sendResponseHeaders(200, resposta.length);
        try (var saida = troca.getResponseBody()) {
            saida.write(resposta);
        }
    }
}
