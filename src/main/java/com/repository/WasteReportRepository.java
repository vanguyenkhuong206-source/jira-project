// WasteReportRepository.java
package com.repository;

import com.entity.WasteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Long> {

    List<WasteReport> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<WasteReport> findByStatusOrderByCreatedAtDesc(String status);

    List<WasteReport> findAllByOrderByCreatedAtDesc();

    @Query("SELECT wr FROM WasteReport wr WHERE wr.status = 'PENDING' ORDER BY wr.createdAt ASC")
    List<WasteReport> findPendingReports();

    @Query("SELECT COUNT(wr) FROM WasteReport wr WHERE wr.citizen.id = :citizenId AND wr.status = :status")
    Long countByCitizenAndStatus(@Param("citizenId") Long citizenId, @Param("status") String status);

    @Query("SELECT wr FROM WasteReport wr WHERE wr.citizen.id = :citizenId ORDER BY wr.createdAt DESC")
    List<WasteReport> findByCitizenId(@Param("citizenId") Long citizenId);
}