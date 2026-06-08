package com.example.bancodb.repository;

import com.example.bancodb.entity.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PARTE 1 - Repositório para ContaBancaria (sem versão).
 */
@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
}
