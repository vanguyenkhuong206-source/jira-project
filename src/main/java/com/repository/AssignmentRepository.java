package com.repository;

import com.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCollectorId(Long collectorId);
    List<Assignment> findByEnterpriseId(Long enterpriseId);
    List<Assignment> findByWasteReportId(Long wasteReportId);
    List<Assignment> findByCollectorIdAndStatus(Long collectorId, String status);
}