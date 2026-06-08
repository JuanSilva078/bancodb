package com.example.bancodb.exception;

/**
 * Exceção lançada quando o saldo da conta é insuficiente para um saque.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String message) {
        super(message);
    }
}
