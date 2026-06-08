package com.example.bancodb.repository;

import com.example.bancodb.entity.ContaBancariaVersionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PARTE 2 - Repositório para ContaBancariaVersionada (com @Version).
 */
@Repository
public interface ContaBancariaVersionadaRepository extends JpaRepository<ContaBancariaVersionada, Long> {
}
