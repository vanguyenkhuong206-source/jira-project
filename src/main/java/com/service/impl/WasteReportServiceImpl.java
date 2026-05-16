package com.service.impl;

import com.dto.request.WasteReportRequest;
import com.entity.User;
import com.entity.WasteReport;
import com.entity.WasteType;
import com.repository.WasteReportRepository;
import com.repository.WasteTypeRepository;
import com.service.NotificationService;
import com.service.RewardService;
import com.service.UserService;
import com.service.WasteReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WasteReportServiceImpl implements WasteReportService {

    private final WasteReportRepository wasteReportRepository;
    private final WasteTypeRepository wasteTypeRepository;
    private final NotificationService notificationService;
    private final RewardService rewardService;
    private final UserService userService;

    public WasteReportServiceImpl(WasteReportRepository wasteReportRepository,
                                   WasteTypeRepository wasteTypeRepository,
                                   NotificationService notificationService,
                                   RewardService rewardService,
                                   UserService userService) {
        this.wasteReportRepository = wasteReportRepository;
        this.wasteTypeRepository = wasteTypeRepository;
        this.notificationService = notificationService;
        this.rewardService = rewardService;
        this.userService = userService;
    }

    @Override
    public WasteReport createReport(WasteReportRequest request, User citizen, String imageUrl) {
        WasteType wasteType = wasteTypeRepository.findById(request.getWasteTypeId())
                .orElseThrow(() -> new RuntimeException("Loại rác không tồn tại!"));

        WasteReport report = new WasteReport();
        report.setCitizen(citizen);
        report.setWasteType(wasteType);
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setAddress(request.getAddress());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setQuantityKg(request.getQuantityKg());
        report.setImageUrl(imageUrl);
        report.setStatus("PENDING");
        report.setRewardPointsEarned(0);
        report.setCitizenNote(request.getCitizenNote());

        WasteReport saved = wasteReportRepository.save(report);

        notificationService.createNotification(citizen,
                "Báo cáo đã được gửi!",
                "Báo cáo \"" + request.getTitle() + "\" đang chờ xử lý.",
                "REPORT_STATUS");

        return saved;
    }

    @Override
    public List<WasteReport> getAllReports() {
        return wasteReportRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<WasteReport> getReportsByStatus(String status) {
        return wasteReportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<WasteReport> getReportsByCitizen(Long citizenId) {
        return wasteReportRepository.findByCitizenId(citizenId);
    }

    @Override
    public Optional<WasteReport> findById(Long id) {
        return wasteReportRepository.findById(id);
    }

    @Override
    public WasteReport updateStatus(Long reportId, String status) {
        WasteReport report = wasteReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Báo cáo không tồn tại!"));

        report.setStatus(status);

        if ("COLLECTED".equals(status)) {
            int points = rewardService.calculatePoints(report);
            report.setRewardPointsEarned(points);
            rewardService.addPoints(report.getCitizen(), report, points,
                    "Điểm thưởng từ: " + report.getTitle());
            userService.addRewardPoints(report.getCitizen().getId(), points);

            notificationService.createNotification(report.getCitizen(),
                    "Rác đã được thu gom!",
                    "Bạn nhận được " + points + " điểm thưởng!",
                    "REWARD");
        } else {
            notificationService.createNotification(report.getCitizen(),
                    "Cập nhật trạng thái",
                    "Báo cáo \"" + report.getTitle() + "\" → " + status,
                    "REPORT_STATUS");
        }

        return wasteReportRepository.save(report);
    }

    @Override
    public void deleteReport(Long id) {
        wasteReportRepository.deleteById(id);
    }

    @Override
    public long countByStatus(String status) {
        return wasteReportRepository.findByStatusOrderByCreatedAtDesc(status).size();
    }
}