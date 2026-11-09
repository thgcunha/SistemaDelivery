package com.example.universidadeESN3.exception;

import com.example.universidadeESN3.controller.RestauranteController;
import com.example.universidadeESN3.service.RestauranteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private RestauranteService restauranteService;

    @InjectMocks
    private RestauranteController restauranteController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(restauranteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void retorna500SemExporDetalhesDaExcecao() throws Exception {
        when(restauranteService.buscarPorId(1L))
                .thenThrow(new IllegalStateException("Detalhe interno do banco de dados"));

        mockMvc.perform(get("/restaurante/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("Ocorreu um erro interno. Tente novamente mais tarde."))
                .andExpect(jsonPath("$.path").value("/restaurante/1"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void preserva400ParaArgumentoInvalido() throws Exception {
        when(restauranteService.buscarPorId(-1L))
                .thenThrow(new IllegalArgumentException("Identificador inválido."));

        mockMvc.perform(get("/restaurante/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Identificador inválido."));
    }
}
