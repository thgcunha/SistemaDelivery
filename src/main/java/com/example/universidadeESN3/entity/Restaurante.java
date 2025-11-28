package com.example.universidadeESN3.entity;



import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "restaurante")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 200)
    private String endereco;

    @Column(length = 15)
    private String telefone;

    @Column(length = 100)
    private String especialidade;

    @Column(nullable = false)
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
