package com.example.universidadeESN3.service;

import com.example.universidadeESN3.dto.CriarPedidoRequest;
import com.example.universidadeESN3.entity.ItemPedido;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PedidoService implements IPedidoService {

    private static final BigDecimal LIMITE_VALOR = new BigDecimal("9999999999.99");

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
    public Pedido criarPedido(CriarPedidoRequest request) {
        if (request.id() != null) {
            throw new IllegalArgumentException("Não informe um ID ao criar um pedido.");
        }
        Pedido pedido = new Pedido();
        pedido.setNomeCliente(request.nomeCliente());
        pedido.setTelefoneCliente(request.telefoneCliente());
        pedido.setEnderecoEntrega(request.enderecoEntrega());
        pedido.setObservacoes(request.observacoes());

        List<ItemPedido> itens = new ArrayList<>();
        Long restauranteId = null;
        BigDecimal total = BigDecimal.ZERO;
        for (CriarPedidoRequest.Item solicitado : request.itens()) {
            Prato prato = pratoRepository.findById(solicitado.pratoId())
                    .orElseThrow(() -> new PratoNotFoundException(solicitado.pratoId()));
            if (!Boolean.TRUE.equals(prato.getAtivo())
                    || !Boolean.TRUE.equals(prato.getDisponivel())
                    || !Boolean.TRUE.equals(prato.getRestaurante().getAtivo())) {
                throw new RegraNegocioException("O prato não está disponível para pedidos: " + solicitado.pratoId());
            }
            Long restauranteDoPrato = prato.getRestaurante().getId();
            if (restauranteId != null && !restauranteId.equals(restauranteDoPrato)) {
                throw new IllegalArgumentException("Todos os pratos do pedido devem pertencer ao mesmo restaurante.");
            }
            restauranteId = restauranteDoPrato;

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setPratoId(prato.getId());
            item.setNomePrato(prato.getNome());
            item.setQuantidade(solicitado.quantidade());
            item.setPrecoUnitario(prato.getPreco());
            item.setSubtotal(prato.getPreco().multiply(BigDecimal.valueOf(solicitado.quantidade())));
            if (item.getSubtotal().compareTo(LIMITE_VALOR) > 0) {
                throw new IllegalArgumentException("O valor do pedido excede o limite permitido.");
            }
            itens.add(item);
            total = total.add(item.getSubtotal());
            if (total.compareTo(LIMITE_VALOR) > 0) {
                throw new IllegalArgumentException("O valor do pedido excede o limite permitido.");
            }
        }

        pedido.setItens(itens);
        pedido.setRestauranteId(restauranteId);
        pedido.setValorTotal(total);
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
