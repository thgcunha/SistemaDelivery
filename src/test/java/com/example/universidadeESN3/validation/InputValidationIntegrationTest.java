package com.example.universidadeESN3.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InputValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("corposInvalidos")
    void rejeitaCamposInvalidos(String rota, String corpo, String mensagem) throws Exception {
        if (rota.equals("/prato")) {
            corpo = corpo.replaceFirst("\\{", "{\"restaurante\":{\"id\":999},");
        }
        mockMvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value(mensagem))
                .andExpect(jsonPath("$.path").value(rota))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    private static Stream<Arguments> corposInvalidos() {
        return Stream.of(
                arguments("/restaurante", "{}", "nome: é obrigatório"),
                arguments("/restaurante", "{\"nome\":\"   \"}", "nome: é obrigatório"),
                arguments("/restaurante", "{\"nome\":\"" + "X".repeat(101) + "\"}",
                        "nome: deve ter no máximo 100 caracteres"),
                arguments("/restaurante", "{\"nome\":\"Teste\",\"telefone\":\"" + "1".repeat(16) + "\"}",
                        "telefone: deve ter no máximo 15 caracteres"),
                arguments("/restaurante", "{\"nome\":\"Teste\",\"ativo\":null}",
                        "ativo: é obrigatório"),
                arguments("/prato", "{\"preco\":10}", "nome: é obrigatório"),
                arguments("/prato", "{\"nome\":\"Teste\"}", "preco: é obrigatório"),
                arguments("/prato", "{\"nome\":\"Teste\",\"preco\":0}",
                        "preco: deve ser pelo menos 0,01"),
                arguments("/prato", "{\"nome\":\"Teste\",\"preco\":-10}",
                        "preco: deve ser pelo menos 0,01"),
                arguments("/prato", "{\"nome\":\"Teste\",\"preco\":10.123}",
                        "preco: deve ter no máximo 10 dígitos inteiros e 2 decimais"),
                arguments("/prato", "{\"nome\":\"Teste\",\"preco\":10,\"tempoPreparoMinutos\":-1}",
                        "tempoPreparoMinutos: não pode ser negativo"),
                arguments("/prato", "{\"nome\":\"Teste\",\"preco\":10,\"descricao\":\""
                        + "X".repeat(501) + "\"}", "descricao: deve ter no máximo 500 caracteres"),
                arguments("/prato", "{\"nome\":\"Teste\",\"preco\":10,\"disponivel\":null}",
                        "disponivel: é obrigatório"),
                arguments("/pedido", pedido("null", "\"11999999999\"", "\"Rua de teste\"", "[{\"pratoId\":999,\"quantidade\":1}]"),
                        "nomeCliente: é obrigatório"),
                arguments("/pedido", pedido("\"Cliente\"", "\" \"", "\"Rua de teste\"", "[{\"pratoId\":999,\"quantidade\":1}]"),
                        "telefoneCliente: é obrigatório"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"", "\" \"", "[{\"pratoId\":999,\"quantidade\":1}]"),
                        "enderecoEntrega: é obrigatório"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"", "\"Rua de teste\"", "[]"),
                        "itens: deve conter ao menos um item"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"", "\"Rua de teste\"", "null"),
                        "itens: deve conter ao menos um item"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"", "\"Rua de teste\"",
                        "[{\"pratoId\":999,\"quantidade\":0}]"),
                        "itens[0].quantidade: deve ser maior que zero"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"", "\"Rua de teste\"",
                        "[{\"pratoId\":999}]"),
                        "itens[0].quantidade: é obrigatória"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"", "\"Rua de teste\"",
                        "[{\"quantidade\":1}]"),
                        "itens[0].pratoId: é obrigatório"),
                arguments("/pedido", pedido("\"Cliente\"", "\"11999999999\"",
                                "\"" + "R".repeat(201) + "\"", "[{\"pratoId\":999,\"quantidade\":1}]"),
                        "enderecoEntrega: deve ter no máximo 200 caracteres")
        );
    }

    @ParameterizedTest
    @MethodSource("identificadoresInvalidos")
    void rejeitaIdentificadoresInvalidos(String metodo, String rota, String corpo, String mensagem) throws Exception {
        if (rota.equals("/prato")) {
            corpo = corpo.replaceFirst("\\{", "{\"restaurante\":{\"id\":999},");
        }
        MockHttpServletRequestBuilder request = metodo.equals("POST") ? post(rota) : put(rota);
        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(mensagem));
    }

    private static Stream<Arguments> identificadoresInvalidos() {
        return Stream.of(
                arguments("POST", "/restaurante", "{\"id\":123,\"nome\":\"Teste\"}",
                        "Não informe um ID ao criar um restaurante."),
                arguments("POST", "/prato", "{\"id\":123,\"nome\":\"Teste\",\"preco\":10}",
                        "Não informe um ID ao criar um prato."),
                arguments("POST", "/pedido", "{\"id\":123," + pedido("\"Cliente\"", "\"11999999999\"",
                                "\"Rua\"", "[{\"pratoId\":999,\"quantidade\":1}]").substring(1),
                        "Não informe um ID ao criar um pedido."),
                arguments("PUT", "/restaurante", "{\"nome\":\"Teste\"}",
                        "Informe o ID do restaurante para atualização."),
                arguments("PUT", "/prato", "{\"nome\":\"Teste\",\"preco\":10}",
                        "Informe o ID do prato para atualização.")
        );
    }

    @Test
    void continuaCriandoRestaurantePratoEPedidoValidos() throws Exception {
        String restauranteJson = mockMvc.perform(post("/restaurante").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Restaurante de teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        long restauranteId = objectMapper.readTree(restauranteJson).get("id").asLong();

        String pratoJson = mockMvc.perform(post("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Prato de teste\",\"preco\":25.90,"
                                + "\"restaurante\":{\"id\":" + restauranteId + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(true))
                .andReturn().getResponse().getContentAsString();
        long pratoId = objectMapper.readTree(pratoJson).get("id").asLong();

        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(pedido("\"Cliente\"", "\"11999999999\"", "\"Rua de teste\"",
                                "[{\"pratoId\":" + pratoId + ",\"quantidade\":1}]")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(25.90))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void rejeitaNomeInvalidoNaAtualizacaoAntesDeConsultarOId() throws Exception {
        mockMvc.perform(put("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"nome\":\" \" ,\"preco\":10,"
                                + "\"restaurante\":{\"id\":999}}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("nome: é obrigatório"));
    }

    private static String pedido(String nome, String telefone, String endereco, String itens) {
        return """
                {"nomeCliente":%s,"telefoneCliente":%s,"enderecoEntrega":%s,"itens":%s}
                """.formatted(nome, telefone, endereco, itens);
    }
}
