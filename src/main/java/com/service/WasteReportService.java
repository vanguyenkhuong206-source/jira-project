// WasteReportService.java
package com.service;

import com.dto.request.WasteReportRequest;
import com.entity.User;
import com.entity.WasteReport;
import java.util.List;
import java.util.Optional;

public interface WasteReportService {
    WasteReport createReport(WasteReportRequest request, User citizen, String imageUrl);
    List<WasteReport> getAllReports();
    List<WasteReport> getReportsByStatus(String status);
    List<WasteReport> getReportsByCitizen(Long citizenId);
    Optional<WasteReport> findById(Long id);
    WasteReport updateStatus(Long reportId, String status);
    void deleteReport(Long id);
    long countByStatus(String status);
}