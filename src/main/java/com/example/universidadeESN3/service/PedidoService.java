package com.example.universidadeESN3.service;

import com.example.universidadeESN3.entity.Pedido;
import com.example.universidadeESN3.entity.Prato;
import com.example.universidadeESN3.entity.StatusPedido;
import com.example.universidadeESN3.exception.PedidoNotFoundException;
import com.example.universidadeESN3.exception.PratoNotFoundException;
import com.example.universidadeESN3.exception.RegraNegocioException;
import com.example.universidadeESN3.repository.PedidoRepository;
import com.example.universidadeESN3.repository.PratoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PedidoService implements IPedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PratoRepository pratoRepository;

    @Override
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
    }

    @Override
    public List<Pedido> buscarTodos() {
        return pedidoRepository.findAll();
    }

    @Override
    @Transactional
    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getId() != null) {
            throw new IllegalArgumentException("Não informe um ID ao criar um pedido.");
        }
        log.info("criarPedido() - pedido: {}", pedido);

        if (pedido.getPratos() != null && !pedido.getPratos().isEmpty()) {
            List<Prato> pratosCompletos = pedido.getPratos().stream()
                    .map(p -> {
                        if (p == null || p.getId() == null) {
                            throw new IllegalArgumentException("Informe o ID de cada prato do pedido.");
                        }
                        Prato prato = pratoRepository.findById(p.getId())
                                .orElseThrow(() -> new PratoNotFoundException(p.getId()));
                        if (!Boolean.TRUE.equals(prato.getAtivo())
                                || !Boolean.TRUE.equals(prato.getDisponivel())
                                || !Boolean.TRUE.equals(prato.getRestaurante().getAtivo())) {
                            throw new RegraNegocioException("O prato não está disponível para pedidos: " + p.getId());
                        }
                        return prato;
                    })
                    .collect(Collectors.toList());

            long restaurantes = pratosCompletos.stream()
                    .map(prato -> prato.getRestaurante().getId())
                    .distinct()
                    .count();
            if (restaurantes != 1) {
                throw new IllegalArgumentException("Todos os pratos do pedido devem pertencer ao mesmo restaurante.");
            }

            pedido.setPratos(pratosCompletos);

            Double total = pratosCompletos.stream()
                    .mapToDouble(Prato::getPreco)
                    .sum();
            pedido.setValorTotal(total);
        }

        pedido.setDataHoraPedido(LocalDateTime.now());
        pedido.setDataHoraEntrega(null);
        pedido.setStatus(StatusPedido.PENDENTE);

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void atualizarStatus(Long id, StatusPedido status) {
        Pedido pedido = buscarPorId(id);
        if (!pedido.getStatus().permiteTransicaoPara(status)) {
            throw new RegraNegocioException("Transição de status inválida: "
                    + pedido.getStatus() + " para " + status);
        }
        pedido.setStatus(status);

        if (status == StatusPedido.ENTREGUE && pedido.getDataHoraEntrega() == null) {
            pedido.setDataHoraEntrega(LocalDateTime.now());
        }

        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void cancelarPedido(Long id) {
        atualizarStatus(id, StatusPedido.CANCELADO);
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
