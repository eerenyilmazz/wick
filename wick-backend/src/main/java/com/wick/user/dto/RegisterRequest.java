package com.wick.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, message = "Şifre en az 8 karakter olmalı")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Şifre en az bir büyük harf, bir küçük harf ve bir sayı içermeli"
        )
        String password,
        @NotBlank String displayName,
        @NotNull Integer acceptedPrivacyPolicyVersion,
        @NotNull Integer acceptedTermsVersion
) {
}
