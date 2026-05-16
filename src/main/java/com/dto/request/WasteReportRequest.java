package com.dto.request;

import jakarta.validation.constraints.*;

public class WasteReportRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;
    private String citizenNote;

    @NotNull(message = "Vui lòng chọn loại rác")
    private Long wasteTypeId;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private Double latitude;
    private Double longitude;
    private Double quantityKg;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCitizenNote() { return citizenNote; }
    public void setCitizenNote(String citizenNote) { this.citizenNote = citizenNote; }


    public Long getWasteTypeId() { return wasteTypeId; }
    public void setWasteTypeId(Long wasteTypeId) { this.wasteTypeId = wasteTypeId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }
}