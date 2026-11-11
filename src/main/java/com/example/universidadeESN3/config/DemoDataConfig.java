package com.example.universidadeESN3.config;

import com.example.universidadeESN3.entity.Prato;
import com.example.universidadeESN3.entity.Restaurante;
import com.example.universidadeESN3.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DemoDataConfig {
    @Bean
    @ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
    CommandLineRunner carregarCatalogoDemo(RestauranteRepository restaurantes, ObjectMapper mapper) {
        return args -> {
            if (restaurantes.count() > 0) {
                return;
            }
            try (InputStream input = new ClassPathResource("static/demo-data.json").getInputStream()) {
                JsonNode dados = mapper.readTree(input);
                for (JsonNode origem : dados.path("restaurantes")) {
                    Restaurante restaurante = new Restaurante();
                    restaurante.setNome(origem.path("nome").asText());
                    restaurante.setEspecialidade(origem.path("especialidade").asText());
                    restaurante.setEndereco(origem.path("endereco").asText());
                    restaurante.setCep(origem.path("cep").asText());
                    restaurante.setLatitude(origem.path("latitude").asDouble());
                    restaurante.setLongitude(origem.path("longitude").asDouble());
                    List<Prato> pratos = new ArrayList<>();
                    for (JsonNode produto : origem.path("pratos")) {
                        Prato prato = new Prato();
                        prato.setNome(produto.path("nome").asText());
                        prato.setDescricao(produto.path("descricao").asText());
                        prato.setPreco(new BigDecimal(produto.path("preco").asText()));
                        prato.setCategoria(produto.path("categoria").asText());
                        prato.setTempoPreparoMinutos(produto.path("tempoPreparoMinutos").asInt());
                        prato.setRestaurante(restaurante);
                        pratos.add(prato);
                    }
                    restaurante.setPratos(pratos);
                    restaurantes.save(restaurante);
                }
            }
        };
    }
}
