package com.example.universidadeESN3.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CriarPedidoRequest(
        Long id,
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String nomeCliente,
        @NotBlank(message = "é obrigatório")
        @Size(max = 15, message = "deve ter no máximo 15 caracteres")
        String telefoneCliente,
        @NotBlank(message = "é obrigatório")
        @Size(max = 200, message = "deve ter no máximo 200 caracteres")
        String enderecoEntrega,
        @NotEmpty(message = "deve conter ao menos um item")
        List<@NotNull(message = "é obrigatório") @Valid Item> itens,
        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String observacoes
) {
    public record Item(
            @NotNull(message = "é obrigatório") Long pratoId,
            @NotNull(message = "é obrigatória")
            @Positive(message = "deve ser maior que zero") Integer quantidade
    ) {
    }
}
