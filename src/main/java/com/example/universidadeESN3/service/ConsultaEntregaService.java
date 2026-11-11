package com.example.universidadeESN3.service;

import com.example.universidadeESN3.entity.Restaurante;
import com.example.universidadeESN3.exception.RestauranteNotFoundException;
import com.example.universidadeESN3.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ConsultaEntregaService {
    private static final BigDecimal TAXA_BASE = new BigDecimal("4.90");
    private static final BigDecimal TAXA_POR_KM = new BigDecimal("1.80");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper mapper;
    private final RestauranteRepository restaurantes;

    @Value("${app.viacep.url:https://viacep.com.br}")
    private String viaCepUrl;

    @Value("${app.brasilapi.url:https://brasilapi.com.br}")
    private String brasilApiUrl;

    @Value("${app.osrm.url:https://router.project-osrm.org}")
    private String osrmUrl;

    public ConsultaEntregaService(ObjectMapper mapper, RestauranteRepository restaurantes) {
        this.mapper = mapper;
        this.restaurantes = restaurantes;
    }

    public record Endereco(String cep, String logradouro, String bairro, String localidade, String uf) {}
    public record Estimativa(double distanciaKm, int duracaoMinutos, BigDecimal frete, String fonte) {}

    public Endereco consultarCep(String cepInformado) {
        String cep = validarCep(cepInformado);
        JsonNode dados = consultar(viaCepUrl + "/ws/" + cep + "/json/");
        if (dados.path("erro").asBoolean(false) || dados.path("cep").isMissingNode()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado.");
        }
        return new Endereco(dados.path("cep").asText(), dados.path("logradouro").asText(),
                dados.path("bairro").asText(), dados.path("localidade").asText(), dados.path("uf").asText());
    }

    public Estimativa estimar(Long restauranteId, String cepInformado) {
        String cep = validarCep(cepInformado);
        Restaurante restaurante = restaurantes.findById(restauranteId)
                .orElseThrow(() -> new RestauranteNotFoundException(restauranteId));
        if (restaurante.getLatitude() == null || restaurante.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Restaurante sem coordenadas para estimar a entrega.");
        }
        JsonNode coordenadas = consultar(brasilApiUrl + "/api/cep/v2/" + cep)
                .path("location").path("coordinates");
        if (coordenadas.path("latitude").isMissingNode() || coordenadas.path("longitude").isMissingNode()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Coordenadas indisponíveis para este CEP.");
        }
        double latitude = coordenadas.path("latitude").asDouble(Double.NaN);
        double longitude = coordenadas.path("longitude").asDouble(Double.NaN);
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Coordenadas indisponíveis para este CEP.");
        }
        String rota = osrmUrl + "/route/v1/driving/" + restaurante.getLongitude() + ","
                + restaurante.getLatitude() + ";" + longitude + "," + latitude + "?overview=false";
        JsonNode primeiraRota = consultar(rota).path("routes").path(0);
        double metros = primeiraRota.path("distance").asDouble(Double.NaN);
        double segundos = primeiraRota.path("duration").asDouble(Double.NaN);
        if (!Double.isFinite(metros) || !Double.isFinite(segundos)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não foi encontrada uma rota para este CEP.");
        }
        double quilometros = Math.round(metros / 100.0) / 10.0;
        BigDecimal frete = calcularFrete(BigDecimal.valueOf(metros).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
        return new Estimativa(quilometros, (int) Math.ceil(segundos / 60), frete, "OSRM/OpenStreetMap");
    }

    public static BigDecimal calcularFrete(BigDecimal distanciaKm) {
        return TAXA_BASE.add(TAXA_POR_KM.multiply(distanciaKm)).setScale(2, RoundingMode.HALF_UP);
    }

    private String validarCep(String cepInformado) {
        String cep = cepInformado == null ? "" : cepInformado.replace("-", "");
        if (!cep.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("O CEP deve conter 8 dígitos.");
        }
        return cep;
    }

    private JsonNode consultar(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .header("User-Agent", "SistemaDeliveryPortfolio/1.0")
                    .GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP ou rota não encontrado.");
            }
            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço externo indisponível.");
            }
            return mapper.readTree(response.body());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço externo indisponível.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Consulta interrompida.", e);
        }
    }
}
