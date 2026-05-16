package com.dto.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, max = 50)
    private String username;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6)
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phone;
    private String address;

    @NotBlank(message = "Vui lòng chọn vai trò")
    private String role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}