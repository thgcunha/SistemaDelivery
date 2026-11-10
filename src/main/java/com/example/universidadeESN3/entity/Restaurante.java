package com.example.universidadeESN3.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "restaurante")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "é obrigatório")
    @Size(max = 100, message = "deve ter no máximo 100 caracteres")
    private String nome;

    @Column(length = 200)
    @Size(max = 200, message = "deve ter no máximo 200 caracteres")
    private String endereco;

    @Column(length = 15)
    @Size(max = 15, message = "deve ter no máximo 15 caracteres")
    private String telefone;

    @Column(length = 100)
    @Size(max = 100, message = "deve ter no máximo 100 caracteres")
    private String especialidade;

    @Column(nullable = false)
    @NotNull(message = "é obrigatório")
    private Boolean ativo = true;

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    private List<Prato> pratos;

    @Override
    public String toString() {
        return "Restaurante{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", especialidade='" + especialidade + '\'' +
                ", ativo=" + ativo +
                '}';
    }
}
