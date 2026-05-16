package com.repository;

import com.entity.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteTypeRepository extends JpaRepository<WasteType, Long> {
    List<WasteType> findByIsActiveTrue();
    List<WasteType> findByCategory(String category);
}