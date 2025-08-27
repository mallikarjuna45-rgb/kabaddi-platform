package com.kabaddi.kabaddi.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUserDto {
    @NotBlank(message ="name is required")
    @Size(min=3,message ="User name must be minimum  3 character")
    private String name;
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character"
    )
    private String password;

    private MultipartFile image;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotBlank(message = "Location cannot be blank")
    private String location;
    @Size(max = 30)
    private String about;
    @Max(value = 10,message = " Cant greater than 10")
    @Min(value = 0,message = "cant less than 0")
    private Float height;
    @Min(value=0,message = "must be greater than 0")
    private Float weight;
    @NotNull(message = "age required")
    private Integer age;



}
