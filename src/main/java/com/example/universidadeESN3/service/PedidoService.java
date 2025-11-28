package com.example.universidadeESN3.service;



import com.example.delivery.entity.Pedido;
import com.example.delivery.entity.StatusPedido;
import com.example.delivery.exception.PedidoNotFoundException;
import com.example.delivery.repository.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PedidoService implements IPedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public Pedido buscarPorId(Long id) {
        try {
            return pedidoRepository.findById(id)
                    .orElseThrow(() -> new PedidoNotFoundException(id));
        } catch (Exception e) {
            log.error("buscarPorId() - ERRO Inesperado: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar o pedido por ID", e);
        }
    }

    @Override
    public List<Pedido> buscarTodos() {
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido criarPedido(Pedido pedido) {
        log.info("criarPedido() - pedido: {}", pedido);
        pedido.setDataHoraPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);

        // Calcular valor total baseado nos pratos
        Double total = pedido.getPratos().stream()
                .mapToDouble(prato -> prato.getPreco())
                .sum();
        pedido.setValorTotal(total);

        return pedidoRepository.save(pedido);
    }

    @Override
    public void atualizarStatus(Long id, StatusPedido status) {
        Pedido pedido = buscarPorId(id);
        pedido.setStatus(status);

        if (status == StatusPedido.ENTREGUE) {
            pedido.setDataHoraEntrega(LocalDateTime.now());
        }

        pedidoRepository.save(pedido);
    }

    @Override
    public void cancelarPedido(Long id) {
        Pedido pedido = buscarPorId(id);
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    public List<Pedido> buscarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    public List<Pedido> buscarPorCliente(String nomeCliente) {
        return pedidoRepository.findByNomeClienteStartingWithIgnoreCase(nomeCliente);
    }

    public Page<Pedido> listarPaginado(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }
}