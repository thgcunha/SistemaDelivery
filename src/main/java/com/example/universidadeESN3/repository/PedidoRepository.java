package com.example.universidadeESN3.repository;



import com.example.universidadeESN3.entity.Pedido;
import com.example.universidadeESN3.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByNomeClienteStartingWithIgnoreCase(String nomeCliente);
    List<Pedido> findByTelefoneCliente(String telefone);
}
