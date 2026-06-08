package com.example.bancodb.controller;

import com.example.bancodb.dto.OperacaoRequest;
import com.example.bancodb.entity.ContaBancaria;
import com.example.bancodb.exception.SaldoInsuficienteException;
import com.example.bancodb.service.ContaBancariaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PARTE 1 - Aluno A
 *
 * Controller para ContaBancaria SEM controle de concorrência.
 *
 * Endpoints:
 *   POST /contas/{id}/deposito  → adiciona valor ao saldo
 *   POST /contas/{id}/saque     → reduz o saldo (com validação)
 *   GET  /contas/{id}           → consulta a conta (para verificar saldo)
 */
@RestController
@RequestMapping("/contas")
public class ContaBancariaController {

    private final ContaBancariaService service;

    public ContaBancariaController(ContaBancariaService service) {
        this.service = service;
    }

    /**
     * Consulta o saldo atual de uma conta.
     * Útil para verificar inconsistências após o teste de concorrência.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContaBancaria> buscar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /contas/{id}/deposito
     * Body: { "valor": 100.00 }
     *
     * Sem controle de concorrência — vulnerável ao Lost Update.
     */
    @PostMapping("/{id}/deposito")
    public ResponseEntity<?> depositar(@PathVariable Long id,
                                       @RequestBody OperacaoRequest request) {
        try {
            ContaBancaria contaAtualizada = service.depositar(id, request.getValor());
            return ResponseEntity.ok(contaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /contas/{id}/saque
     * Body: { "valor": 50.00 }
     *
     * Valida saldo negativo, mas sem proteção contra race condition.
     */
    @PostMapping("/{id}/saque")
    public ResponseEntity<?> sacar(@PathVariable Long id,
                                   @RequestBody OperacaoRequest request) {
        try {
            ContaBancaria contaAtualizada = service.sacar(id, request.getValor());
            return ResponseEntity.ok(contaAtualizada);
        } catch (SaldoInsuficienteException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
