package com.example.universidadeESN3.repository;



import com.example.delivery.entity.Pedido;
import com.example.delivery.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByNomeClienteStartingWithIgnoreCase(String nomeCliente);
    List<Pedido> findByTelefoneCliente(String telefone);
}
