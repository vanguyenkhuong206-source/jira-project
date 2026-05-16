// WasteReportResponse.java
package com.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WasteReportResponse {
    private Long id;
    private String title;
    private String description;
    private String address;
    private String status;
    private String wasteTypeName;
    private String wasteTypeCategory;
    private String citizenName;
    private Double quantityKg;
    private Integer rewardPointsEarned;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}