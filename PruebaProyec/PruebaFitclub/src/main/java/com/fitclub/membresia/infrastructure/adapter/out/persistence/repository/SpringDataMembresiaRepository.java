package com.fitclub.membresia.infrastructure.adapter.out.persistence.repository;

import com.fitclub.membresia.infrastructure.adapter.out.persistence.entity.MembresiaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMembresiaRepository extends JpaRepository<MembresiaJpaEntity, Long> {

    List<MembresiaJpaEntity> findBySocioId(Long socioId);
}