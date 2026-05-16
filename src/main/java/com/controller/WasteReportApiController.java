package com.controller;

import com.dto.response.ApiResponse;
import com.entity.WasteReport;
import com.service.WasteReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class WasteReportApiController {

    private final WasteReportService wasteReportService;

    public WasteReportApiController(WasteReportService wasteReportService) {
        this.wasteReportService = wasteReportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WasteReport>>> getAllReports() {
        List<WasteReport> reports = wasteReportService.getAllReports();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", reports));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WasteReport>> getById(@PathVariable Long id) {
        return wasteReportService.findById(id)
                .map(r -> ResponseEntity.ok(ApiResponse.success("Thành công", r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<WasteReport>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        WasteReport updated = wasteReportService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        wasteReportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thành công", null));
    }
}