package com.example.universidadeESN3.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "é obrigatório")
    @Size(max = 100, message = "deve ter no máximo 100 caracteres")
    private String nomeCliente;

    @Column(nullable = false, length = 15)
    @NotBlank(message = "é obrigatório")
    @Size(max = 15, message = "deve ter no máximo 15 caracteres")
    private String telefoneCliente;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "é obrigatório")
    @Size(max = 200, message = "deve ter no máximo 200 caracteres")
    private String enderecoEntrega;

    @ManyToMany
    @JoinTable(
            name = "pedido_prato",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "prato_id")
    )
    @NotEmpty(message = "deve conter ao menos um prato")
    private List<Prato> pratos;

    @Column(nullable = false)
    private Double valorTotal;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataHoraPedido = LocalDateTime.now();

    @Column
    private LocalDateTime dataHoraEntrega;

    @Column(length = 500)
    @Size(max = 500, message = "deve ter no máximo 500 caracteres")
    private String observacoes;

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", nomeCliente='" + nomeCliente + '\'' +
                ", valorTotal=" + valorTotal +
                ", status=" + status +
                ", dataHoraPedido=" + dataHoraPedido +
                '}';
    }
}
