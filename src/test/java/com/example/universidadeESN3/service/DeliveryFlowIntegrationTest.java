package com.example.universidadeESN3.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void vinculaPratoAoRestauranteNasConsultas() throws Exception {
        long restauranteId = restaurante(true);
        long pratoId = prato(restauranteId, true);

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/prato/restaurante/{id}", restauranteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pratoId))
                .andExpect(jsonPath("$[0].restaurante.id").value(restauranteId));
        mockMvc.perform(get("/restaurante/{id}", restauranteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pratos[0].id").value(pratoId));
    }

    @Test
    void cadastroDeRestauranteComPratoPersisteOVinculo() throws Exception {
        String json = mockMvc.perform(post("/restaurante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Restaurante com cardápio\",\"pratos\":[{\"nome\":\"Prato incluído\",\"preco\":19.9}]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long restauranteId = objectMapper.readTree(json).path("id").asLong();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/prato/restaurante/{id}", restauranteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Prato incluído"))
                .andExpect(jsonPath("$[0].restaurante.id").value(restauranteId));
    }

    @Test
    void recusaRestauranteAusenteOuInativoNoCadastroDePrato() throws Exception {
        mockMvc.perform(post("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"preco\":10}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("restaurante: é obrigatório"));

        mockMvc.perform(post("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPrato(999999L, true)))
                .andExpect(status().isNotFound());

        long restauranteInativo = restaurante(false);
        mockMvc.perform(post("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPrato(restauranteInativo, true)))
                .andExpect(status().isConflict());
    }

    @Test
    void recusaPratoIndisponivelOuInativoNoPedido() throws Exception {
        long restauranteId = restaurante(true);
        long pratoId = prato(restauranteId, false);

        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(pratoId)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/prato/{id}/disponibilidade", pratoId).param("disponivel", "true"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(pratoId)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/prato/inactive/{id}", pratoId)).andExpect(status().isOk());
        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(pratoId)))
                .andExpect(status().isConflict());

        long outroPrato = prato(restauranteId, true);
        mockMvc.perform(delete("/restaurante/inactive/{id}", restauranteId)).andExpect(status().isOk());
        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(outroPrato)))
                .andExpect(status().isConflict());
    }

    @Test
    void pedidoAceitaApenasPratosDoMesmoRestaurante() throws Exception {
        long primeiroPrato = prato(restaurante(true), true);
        long segundoPrato = prato(restaurante(true), true);

        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPedido(primeiroPrato, segundoPrato)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Todos os pratos do pedido devem pertencer ao mesmo restaurante."));
    }

    @Test
    void statusSegueFluxoERegistraEntregaUmaVez() throws Exception {
        long pedidoId = pedido(prato(restaurante(true), true));

        mudarStatus(pedidoId, "ENTREGUE", 409);
        mudarStatus(pedidoId, "CONFIRMADO", 200);
        mudarStatus(pedidoId, "EM_PREPARO", 200);
        mudarStatus(pedidoId, "SAIU_PARA_ENTREGA", 200);
        mudarStatus(pedidoId, "ENTREGUE", 200);

        JsonNode entregue = consultarPedido(pedidoId);
        assertThat(entregue.path("dataHoraEntrega").isNull()).isFalse();

        mudarStatus(pedidoId, "ENTREGUE", 200);
        assertThat(consultarPedido(pedidoId).path("dataHoraEntrega").asText())
                .isEqualTo(entregue.path("dataHoraEntrega").asText());

        mockMvc.perform(delete("/pedido/{id}", pedidoId)).andExpect(status().isConflict());
        mudarStatus(pedidoId, "PENDENTE", 409);
        assertThat(consultarPedido(pedidoId).path("status").asText()).isEqualTo("ENTREGUE");
    }

    @Test
    void cancelamentoEFinalEPodeSerRepetido() throws Exception {
        long pedidoId = pedido(prato(restaurante(true), true));

        mockMvc.perform(delete("/pedido/{id}", pedidoId)).andExpect(status().isOk());
        mockMvc.perform(delete("/pedido/{id}", pedidoId)).andExpect(status().isOk());
        mudarStatus(pedidoId, "CONFIRMADO", 409);
        assertThat(consultarPedido(pedidoId).path("status").asText()).isEqualTo("CANCELADO");
    }

    @Test
    void pedidoNovoIgnoraStatusEDataDeEntregaInformadosPeloCliente() throws Exception {
        long pratoId = prato(restaurante(true), true);
        String corpo = corpoPedido(pratoId);
        corpo = corpo.substring(0, corpo.length() - 1)
                + ",\"status\":\"ENTREGUE\",\"dataHoraEntrega\":\"2026-11-10T10:00:00\",\"valorTotal\":0.01}";

        mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.valorTotal").value(25.9))
                .andExpect(jsonPath("$.dataHoraEntrega").isEmpty());
    }

    @Test
    void quantidadeEPrecoHistoricoPermanecemAposEdicaoDoPrato() throws Exception {
        long restauranteId = restaurante(true);
        String pratoJson = mockMvc.perform(post("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Prato original\",\"preco\":0.10,\"restaurante\":{\"id\":"
                                + restauranteId + "}}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long pratoId = objectMapper.readTree(pratoJson).path("id").asLong();

        String corpo = corpoPedido(pratoId).replace("\"quantidade\":1", "\"quantidade\":3");
        String pedidoJson = mockMvc.perform(post("/pedido").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(0.3))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(0.1))
                .andExpect(jsonPath("$.itens[0].subtotal").value(0.3))
                .andReturn().getResponse().getContentAsString();
        long pedidoId = objectMapper.readTree(pedidoJson).path("id").asLong();

        mockMvc.perform(put("/prato").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + pratoId + ",\"nome\":\"Prato atualizado\",\"preco\":99.90,"
                                + "\"restaurante\":{\"id\":" + restauranteId + "}}"))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/pedido/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(0.3))
                .andExpect(jsonPath("$.itens[0].nomePrato").value("Prato original"))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(0.1))
                .andExpect(jsonPath("$.itens[0].subtotal").value(0.3));
    }

    private long restaurante(boolean ativo) throws Exception {
        String body = "{\"nome\":\"Restaurante de teste\",\"ativo\":" + ativo + "}";
        String json = mockMvc.perform(post("/restaurante")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("id").asLong();
    }

    private long prato(long restauranteId, boolean disponivel) throws Exception {
        String json = mockMvc.perform(post("/prato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPrato(restauranteId, disponivel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurante.id").value(restauranteId))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("id").asLong();
    }

    private long pedido(long pratoId) throws Exception {
        String json = mockMvc.perform(post("/pedido")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoPedido(pratoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(25.9))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).path("id").asLong();
    }

    private JsonNode consultarPedido(long id) throws Exception {
        String json = mockMvc.perform(get("/pedido/{id}", id))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json);
    }

    private void mudarStatus(long id, String status, int esperado) throws Exception {
        mockMvc.perform(patch("/pedido/{id}/status", id).param("status", status))
                .andExpect(status().is(esperado));
    }

    private String corpoPrato(long restauranteId, boolean disponivel) {
        return "{\"nome\":\"Prato de teste\",\"preco\":25.9,\"disponivel\":" + disponivel
                + ",\"restaurante\":{\"id\":" + restauranteId + "}}";
    }

    private String corpoPedido(long... pratos) {
        StringBuilder ids = new StringBuilder();
        for (long id : pratos) {
            if (!ids.isEmpty()) {
                ids.append(',');
            }
            ids.append("{\"pratoId\":").append(id).append(",\"quantidade\":1}");
        }
        return "{\"nomeCliente\":\"Cliente\",\"telefoneCliente\":\"11999999999\","
                + "\"enderecoEntrega\":\"Rua de teste\",\"itens\":[" + ids + "]}";
    }
}
