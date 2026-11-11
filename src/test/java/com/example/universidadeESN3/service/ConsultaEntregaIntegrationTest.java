package com.example.universidadeESN3.service;

import com.example.universidadeESN3.entity.Restaurante;
import com.example.universidadeESN3.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsultaEntregaIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private RestauranteRepository restaurantes;

    @Test
    void validaCepAntesDeConsultarServicoExterno() throws Exception {
        mvc.perform(get("/api/cep/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O CEP deve conter 8 dígitos."));
    }

    @Test
    void informaQuandoRestauranteNaoTemCoordenadas() throws Exception {
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Sem localização");
        restaurante = restaurantes.save(restaurante);

        mvc.perform(get("/api/entrega/estimativa")
                        .param("restauranteId", restaurante.getId().toString())
                        .param("cep", "01001000"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Restaurante sem coordenadas para estimar a entrega."));
    }

    @Test
    void calculaFreteComArredondamentoMonetario() {
        assertThat(ConsultaEntregaService.calcularFrete(new BigDecimal("3.25")))
                .isEqualByComparingTo("10.75");
    }
}
