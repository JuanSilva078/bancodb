package com.example.bancodb.dto;

import java.math.BigDecimal;

/**
 * DTO para receber o valor de uma operação (depósito ou saque) via JSON.
 */
public class OperacaoRequest {

    private BigDecimal valor;

    public OperacaoRequest() {}

    public OperacaoRequest(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
