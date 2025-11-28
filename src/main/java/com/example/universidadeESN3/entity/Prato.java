package com.example.universidadeESN3.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "prato")
public class Prato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private Double preco;

    @Column(length = 50)
    private String categoria;

    @Column
    private Integer tempoPreparoMinutos;

    @Column(nullable = false)
    private Boolean disponivel = true;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id")
    @JsonIgnore
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
