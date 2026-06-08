package com.example.bancodb.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * PARTE 2 - Aluno B
 * Entidade ContaBancariaVersionada COM controle de versão otimista (@Version).
 *
 * O campo `version` é gerenciado automaticamente pelo Hibernate.
 * A cada UPDATE bem-sucedido, o Hibernate incrementa esse número.
 * Se duas transações tentarem atualizar o mesmo registro com a mesma versão,
 * a segunda lançará ObjectOptimisticLockingFailureException — evitando o Lost Update.
 */
@Entity
@Table(name = "conta_bancaria_versionada")
public class ContaBancariaVersionada {

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

    /**
     * Campo de versão para Locking Otimista.
     * O Hibernate gerencia esse campo automaticamente:
     *   - Lê a versão antes de cada UPDATE.
     *   - Incrementa após cada UPDATE bem-sucedido.
     *   - Lança exceção se a versão não corresponder (conflito detectado).
     */
    @Version
    @Column(nullable = false)
    private Integer version;

    public ContaBancariaVersionada() {}

    public ContaBancariaVersionada(String titular, BigDecimal saldo) {
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

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public String toString() {
        return "ContaBancariaVersionada{id=" + id + ", titular='" + titular
                + "', saldo=" + saldo + ", version=" + version + "}";
    }
}
