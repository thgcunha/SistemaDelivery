package com.example.universidadeESN3.service;

import com.example.universidadeESN3.entity.Pedido;
import com.example.universidadeESN3.entity.Prato;
import com.example.universidadeESN3.entity.StatusPedido;
import com.example.universidadeESN3.exception.PedidoNotFoundException;
import com.example.universidadeESN3.repository.PedidoRepository;
import com.example.universidadeESN3.repository.PratoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PedidoService implements IPedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PratoRepository pratoRepository;  // ← PRECISA INJETAR O REPOSITORY DE PRATO!

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

        // ← CORREÇÃO: Buscar os pratos completos do banco de dados
        if (pedido.getPratos() != null && !pedido.getPratos().isEmpty()) {
            List<Prato> pratosCompletos = pedido.getPratos().stream()
                    .map(p -> pratoRepository.findById(p.getId())
                            .orElseThrow(() -> new RuntimeException("Prato não encontrado: " + p.getId())))
                    .collect(Collectors.toList());

            pedido.setPratos(pratosCompletos);

            // ← CORREÇÃO: Calcular valor total DEPOIS de ter os pratos completos
            Double total = pratosCompletos.stream()
                    .mapToDouble(Prato::getPreco)
                    .sum();
            pedido.setValorTotal(total);
        }

        pedido.setDataHoraPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);

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