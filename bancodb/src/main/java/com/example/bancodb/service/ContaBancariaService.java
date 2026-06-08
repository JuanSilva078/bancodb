package com.example.bancodb.service;

import com.example.bancodb.entity.ContaBancaria;
import com.example.bancodb.exception.SaldoInsuficienteException;
import com.example.bancodb.repository.ContaBancariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * PARTE 1 - Aluno A
 *
 * Service SEM controle de concorrência.
 *
 * Problema intencional (Lost Update / Atualização Perdida):
 *   1. Thread A lê saldo = R$ 1000
 *   2. Thread B lê saldo = R$ 1000 (ao mesmo tempo)
 *   3. Thread A soma R$ 100 → salva R$ 1100
 *   4. Thread B soma R$ 100 → salva R$ 1100 (sobrescreve Thread A!)
 *   Resultado: saldo = R$ 1100 (deveria ser R$ 1200)
 *
 * O @Transactional aqui garante atomicidade da operação individualmente,
 * mas NÃO evita que duas transações paralelas leiam o mesmo valor antigo
 * antes de qualquer uma delas commitar.
 */
@Service
public class ContaBancariaService {

    private final ContaBancariaRepository repository;

    public ContaBancariaService(ContaBancariaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca uma conta pelo ID.
     */
    public ContaBancaria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada com id: " + id));
    }

    /**
     * Deposita um valor na conta SEM controle de concorrência.
     * Vulnerável ao problema de Lost Update com múltiplas threads simultâneas.
     */
    @Transactional
    public ContaBancaria depositar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        ContaBancaria conta = buscarPorId(id);

        // ⚠️ PONTO CRÍTICO: leitura do saldo atual
        // Em ambiente concorrente, duas threads podem ler o mesmo valor aqui
        BigDecimal novoSaldo = conta.getSaldo().add(valor);
        conta.setSaldo(novoSaldo);

        return repository.save(conta);
    }

    /**
     * Saca um valor da conta SEM controle de concorrência.
     * Valida se o saldo é suficiente, mas não previne race condition.
     */
    @Transactional
    public ContaBancaria sacar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser positivo.");
        }

        ContaBancaria conta = buscarPorId(id);

        // Validação de saldo
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Saldo atual: R$ " + conta.getSaldo()
                    + " | Valor solicitado: R$ " + valor);
        }

        // ⚠️ PONTO CRÍTICO: mesmo problema ocorre aqui com saques concorrentes
        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);
        conta.setSaldo(novoSaldo);

        return repository.save(conta);
    }
}
