package com.example.universidadeESN3.entity;

public enum StatusPedido {
    PENDENTE,
    CONFIRMADO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;

    public boolean permiteTransicaoPara(StatusPedido proximo) {
        if (this == proximo) {
            return true;
        }
        return switch (this) {
            case PENDENTE -> proximo == CONFIRMADO || proximo == CANCELADO;
            case CONFIRMADO -> proximo == EM_PREPARO || proximo == CANCELADO;
            case EM_PREPARO -> proximo == SAIU_PARA_ENTREGA || proximo == CANCELADO;
            case SAIU_PARA_ENTREGA -> proximo == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
