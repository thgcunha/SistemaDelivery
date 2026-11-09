package com.example.universidadeESN3.exception;

import com.example.universidadeESN3.entity.Restaurante;
import com.example.universidadeESN3.repository.PedidoRepository;
import com.example.universidadeESN3.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiErrorIntegrationTest {

    private static final long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @ParameterizedTest
    @CsvSource({
            "GET, /restaurante/9223372036854775807, Restaurante",
            "GET, /prato/9223372036854775807, Prato",
            "GET, /pedido/9223372036854775807, Pedido",
            "DELETE, /restaurante/9223372036854775807, Restaurante",
            "DELETE, /restaurante/inactive/9223372036854775807, Restaurante",
            "DELETE, /prato/9223372036854775807, Prato",
            "DELETE, /prato/inactive/9223372036854775807, Prato",
            "DELETE, /pedido/9223372036854775807, Pedido",
            "PATCH, /pedido/9223372036854775807/status?status=CONFIRMADO, Pedido",
            "PATCH, /prato/9223372036854775807/disponibilidade?disponivel=false, Prato"
    })
    void retorna404ParaRecursoInexistente(String metodo, String rota, String recurso) throws Exception {
        String caminho = rota.split("\\?", 2)[0];

        verificarErro(mockMvc.perform(request(HttpMethod.valueOf(metodo), rota)),
                404, "Not Found", caminho,
                recurso + " não encontrado com o ID: " + ID_INEXISTENTE);
    }

    @ParameterizedTest
    @CsvSource({"/restaurante, Restaurante", "/prato, Prato"})
    void retorna404AoAtualizarRecursoInexistente(String rota, String recurso) throws Exception {
        verificarErro(mockMvc.perform(put(rota)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + ID_INEXISTENTE + ",\"nome\":\"Teste\"}")),
                404, "Not Found", rota,
                recurso + " não encontrado com o ID: " + ID_INEXISTENTE);
    }

    @Test
    void retorna404SemCriarPedidoQuandoPratoNaoExiste() throws Exception {
        long quantidadeAntes = pedidoRepository.count();
        String pedido = """
                {
                  "nomeCliente": "Cliente de teste",
                  "telefoneCliente": "11999999999",
                  "enderecoEntrega": "Endereço de teste",
                  "pratos": [{"id": %d}]
                }
                """.formatted(ID_INEXISTENTE);

        verificarErro(mockMvc.perform(post("/pedido")
                        .contentType(MediaType.APPLICATION_JSON).content(pedido)),
                404, "Not Found", "/pedido",
                "Prato não encontrado com o ID: " + ID_INEXISTENTE);

        assertThat(pedidoRepository.count()).isEqualTo(quantidadeAntes);
    }

    @ParameterizedTest
    @CsvSource({
            "/restaurante/abc, id",
            "/prato/abc, id",
            "/pedido/abc, id",
            "/pedido/status/INVALIDO, status"
    })
    void retorna400ParaParametroDeRotaInvalido(String rota, String parametro) throws Exception {
        verificarErro(mockMvc.perform(get(rota)), 400, "Bad Request", rota,
                "O parâmetro '" + parametro + "' possui um valor inválido.");
    }

    @ParameterizedTest
    @CsvSource({
            "/pedido/1/status, status",
            "/prato/1/disponibilidade, disponivel"
    })
    void retorna400ParaParametroDeConsultaInvalido(String rota, String parametro) throws Exception {
        verificarErro(mockMvc.perform(patch(rota).param(parametro, "INVALIDO")),
                400, "Bad Request", rota,
                "O parâmetro '" + parametro + "' possui um valor inválido.");
    }

    @ParameterizedTest
    @CsvSource({
            "/pedido/1/status, status",
            "/prato/1/disponibilidade, disponivel"
    })
    void retorna400ParaParametroObrigatorioAusente(String rota, String parametro) throws Exception {
        verificarErro(mockMvc.perform(patch(rota)), 400, "Bad Request", rota,
                "O parâmetro '" + parametro + "' é obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/restaurante", "/prato", "/pedido"})
    void retorna400ParaJsonMalformado(String rota) throws Exception {
        verificarCorpoInvalido(mockMvc.perform(post(rota)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":")), rota);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/restaurante", "/prato", "/pedido"})
    void retorna400ParaCorpoAusente(String rota) throws Exception {
        verificarCorpoInvalido(mockMvc.perform(post(rota)
                .contentType(MediaType.APPLICATION_JSON)), rota);
    }

    @Test
    void retorna400ParaTipoInvalidoNoJson() throws Exception {
        verificarCorpoInvalido(mockMvc.perform(post("/prato")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Prato de teste\",\"preco\":\"abc\"}")), "/prato");
    }

    @Test
    void retorna404ParaRotaInexistente() throws Exception {
        verificarErro(mockMvc.perform(get("/rota-inexistente")), 404, "Not Found",
                "/rota-inexistente", "Recurso não encontrado.");
    }

    @Test
    void preserva405EHeaderAllowParaMetodoNaoPermitido() throws Exception {
        verificarErro(mockMvc.perform(put("/pedido/1")), 405, "Method Not Allowed",
                "/pedido/1", "Método HTTP não permitido para este recurso.")
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(header().string("Allow", containsString("DELETE")));
    }

    @Test
    void preserva415ParaTipoDeConteudoNaoSuportado() throws Exception {
        verificarErro(mockMvc.perform(post("/restaurante")
                        .contentType(MediaType.TEXT_PLAIN).content("{}")),
                415, "Unsupported Media Type", "/restaurante", "Tipo de conteúdo não suportado.");
    }

    @Test
    void continuaRetornandoRestauranteExistente() throws Exception {
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante de teste");
        restaurante = restauranteRepository.saveAndFlush(restaurante);

        mockMvc.perform(get("/restaurante/{id}", restaurante.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(restaurante.getId()))
                .andExpect(jsonPath("$.nome").value("Restaurante de teste"));
    }

    private void verificarCorpoInvalido(ResultActions resultado, String caminho) throws Exception {
        verificarErro(resultado, 400, "Bad Request", caminho,
                "O corpo da requisição está ausente ou possui formato inválido.");
    }

    private ResultActions verificarErro(ResultActions resultado, int codigo, String erro,
                                        String caminho, String mensagem) throws Exception {
        return resultado.andExpect(status().is(codigo))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(codigo))
                .andExpect(jsonPath("$.error").value(erro))
                .andExpect(jsonPath("$.message").value(mensagem))
                .andExpect(jsonPath("$.path").value(caminho))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
