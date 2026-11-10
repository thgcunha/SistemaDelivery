package com.example.universidadeESN3.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "prato")
public class Prato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "é obrigatório")
    @Size(max = 100, message = "deve ter no máximo 100 caracteres")
    private String nome;

    @Column(length = 500)
    @Size(max = 500, message = "deve ter no máximo 500 caracteres")
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "é obrigatório")
    @DecimalMin(value = "0.01", message = "deve ser pelo menos 0,01")
    @Digits(integer = 10, fraction = 2, message = "deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal preco;

    @Column(length = 50)
    @Size(max = 50, message = "deve ter no máximo 50 caracteres")
    private String categoria;

    @Column
    @PositiveOrZero(message = "não pode ser negativo")
    private Integer tempoPreparoMinutos;

    @Column(nullable = false)
    @NotNull(message = "é obrigatório")
    private Boolean disponivel = true;

    @Column(nullable = false)
    @NotNull(message = "é obrigatório")
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    @NotNull(message = "é obrigatório")
    @JsonIgnoreProperties({"pratos", "hibernateLazyInitializer", "handler"})
    private Restaurante restaurante;

    @Override
    public String toString() {
        return "Prato{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                ", categoria='" + categoria + '\'' +
                ", disponivel=" + disponivel +
                ", ativo=" + ativo +
                '}';
    }
}
