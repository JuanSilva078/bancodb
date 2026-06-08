package com.example.bancodb.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * PARTE 1 - Aluno A
 * Entidade ContaBancaria SEM controle de versão.
 * Demonstra o problema de Lost Update (Atualização Perdida) em acessos concorrentes.
 */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titular;

    /**
     * Atributo monetário utiliza BigDecimal conforme exigido pela atividade.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    public ContaBancaria() {}

    public ContaBancaria(String titular, BigDecimal saldo) {
        this.titular = titular;
        this.saldo = saldo;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    @Override
    public String toString() {
        return "ContaBancaria{id=" + id + ", titular='" + titular + "', saldo=" + saldo + "}";
    }
}
