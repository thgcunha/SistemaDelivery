package com.example.universidadeESN3.entity;

import jakarta.persistence.*;
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
    private String nomeCliente;

    @Column(nullable = false, length = 15)
    private String telefoneCliente;

    @Column(nullable = false, length = 200)
    private String enderecoEntrega;

    @ManyToMany
    @JoinTable(
            name = "pedido_prato",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "prato_id")
    )
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
