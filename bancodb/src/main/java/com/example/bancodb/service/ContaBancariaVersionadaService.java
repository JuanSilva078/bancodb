package com.example.bancodb.service;

import com.example.bancodb.entity.ContaBancariaVersionada;
import com.example.bancodb.exception.SaldoInsuficienteException;
import com.example.bancodb.repository.ContaBancariaVersionadaRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * PARTE 2 - Aluno B
 *
 * Service COM Locking Otimista (@Version).
 *
 * Como o Locking Otimista resolve o Lost Update:
 *   1. Thread A lê conta: saldo = R$ 1000, version = 0
 *   2. Thread B lê conta: saldo = R$ 1000, version = 0
 *   3. Thread A soma R$ 100 → UPDATE ... WHERE version = 0 → OK! version vira 1
 *   4. Thread B tenta: UPDATE ... WHERE version = 0 → FALHA! version já é 1
 *      → Hibernate lança ObjectOptimisticLockingFailureException
 *   Resultado: saldo correto = R$ 1100, e Thread B retorna HTTP 409 Conflict
 *
 * Vantagem: não bloqueia o banco (sem locks pessimistas),
 * apenas detecta conflitos na hora do commit e rejeita o perdedor.
 */
@Service
public class ContaBancariaVersionadaService {

    private final ContaBancariaVersionadaRepository repository;

    public ContaBancariaVersionadaService(ContaBancariaVersionadaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca uma conta versionada pelo ID.
     */
    public ContaBancariaVersionada buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta versionada não encontrada com id: " + id));
    }

    /**
     * Deposita um valor na conta COM controle de versão otimista.
     *
     * Se outra transação commitar antes desta (mesma version),
     * o Hibernate detecta o conflito e lança ObjectOptimisticLockingFailureException,
     * que é propagada para o controller tratar com HTTP 409.
     *
     * @throws ObjectOptimisticLockingFailureException se houver conflito de versão
     */
    @Transactional
    public ContaBancariaVersionada depositar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        ContaBancariaVersionada conta = buscarPorId(id);

        BigDecimal novoSaldo = conta.getSaldo().add(valor);
        conta.setSaldo(novoSaldo);

        // O Hibernate verificará a version automaticamente no UPDATE.
        // Se outra transação já atualizou o registro, uma exceção será lançada.
        return repository.save(conta);
    }

    /**
     * Saca um valor da conta COM controle de versão otimista.
     *
     * @throws SaldoInsuficienteException se o saldo for insuficiente
     * @throws ObjectOptimisticLockingFailureException se houver conflito de versão
     */
    @Transactional
    public ContaBancariaVersionada sacar(Long id, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser positivo.");
        }

        ContaBancariaVersionada conta = buscarPorId(id);

        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Saldo atual: R$ " + conta.getSaldo()
                    + " | Valor solicitado: R$ " + valor);
        }

        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);
        conta.setSaldo(novoSaldo);

        return repository.save(conta);
    }
}
