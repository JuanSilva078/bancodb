package com.example.bancodb.controller;

import com.example.bancodb.dto.OperacaoRequest;
import com.example.bancodb.entity.ContaBancariaVersionada;
import com.example.bancodb.exception.SaldoInsuficienteException;
import com.example.bancodb.service.ContaBancariaVersionadaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PARTE 2 - Aluno B
 *
 * Controller para ContaBancariaVersionada COM controle de versão otimista.
 *
 * Endpoints:
 *   POST /contas-versionadas/{id}/deposito  → adiciona valor (com proteção @Version)
 *   POST /contas-versionadas/{id}/saque     → reduz o saldo (com proteção @Version)
 *   GET  /contas-versionadas/{id}           → consulta a conta versionada
 *
 * Diferença principal: trata ObjectOptimisticLockingFailureException
 * retornando HTTP 409 Conflict com mensagem amigável.
 */
@RestController
@RequestMapping("/contas-versionadas")
public class ContaBancariaVersionadaController {

    private final ContaBancariaVersionadaService service;

    public ContaBancariaVersionadaController(ContaBancariaVersionadaService service) {
        this.service = service;
    }

    /**
     * Consulta o saldo e a versão atual de uma conta versionada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContaBancariaVersionada> buscar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /contas-versionadas/{id}/deposito
     * Body: { "valor": 100.00 }
     *
     * COM controle de versão otimista.
     * Em caso de conflito → HTTP 409 Conflict.
     */
    @PostMapping("/{id}/deposito")
    public ResponseEntity<?> depositar(@PathVariable Long id,
                                       @RequestBody OperacaoRequest request) {
        try {
            ContaBancariaVersionada contaAtualizada = service.depositar(id, request.getValor());
            return ResponseEntity.ok(contaAtualizada);

        } catch (ObjectOptimisticLockingFailureException e) {
            // Conflito de versão detectado pelo Hibernate
            // Retorna 409 Conflict com mensagem explicativa
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro", "Conflito de concorrência detectado.",
                            "mensagem", "Outro processo atualizou esta conta simultaneamente. Por favor, tente novamente.",
                            "status", 409
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /contas-versionadas/{id}/saque
     * Body: { "valor": 50.00 }
     *
     * COM controle de versão otimista.
     * Em caso de conflito → HTTP 409 Conflict.
     */
    @PostMapping("/{id}/saque")
    public ResponseEntity<?> sacar(@PathVariable Long id,
                                   @RequestBody OperacaoRequest request) {
        try {
            ContaBancariaVersionada contaAtualizada = service.sacar(id, request.getValor());
            return ResponseEntity.ok(contaAtualizada);

        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "erro", "Conflito de concorrência detectado.",
                            "mensagem", "Outro processo atualizou esta conta simultaneamente. Por favor, tente novamente.",
                            "status", 409
                    ));

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
