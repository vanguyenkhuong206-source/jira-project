package com.repository;

import com.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {
    Optional<Collector> findByUserId(Long userId);
    List<Collector> findByEnterpriseId(Long enterpriseId);
    List<Collector> findByIsAvailableTrue();
    List<Collector> findByEnterpriseIdAndIsAvailableTrue(Long enterpriseId);
}