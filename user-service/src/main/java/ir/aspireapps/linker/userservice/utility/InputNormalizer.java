package ir.aspireapps.linker.userservice.utility;

import ir.aspireapps.linker.userservice.dto.UserLoginRequest;
import ir.aspireapps.linker.userservice.dto.UserRegisterRequest;
import ir.aspireapps.linker.userservice.form.UserLoginForm;
import ir.aspireapps.linker.userservice.form.UserRegisterForm;

import java.util.Locale;

public abstract class InputNormalizer {
    public static String normalizeString(String input) {
        return input == null
                ? null
                : input.trim().toLowerCase(Locale.ROOT);
    }

    public static UserRegisterRequest normalize(UserRegisterRequest request) {
        return UserRegisterRequest.builder()
                .username(normalizeString(request.username()))
                .email(normalizeString(request.email()))
                .password(request.password())
                .passwordConfirm(request.passwordConfirm())
                .build();
    }

    public static UserLoginRequest normalize(UserLoginRequest request) {
        return UserLoginRequest.builder()
                .username(normalizeString(request.username()))
                .password(normalizeString(request.password()))
                .build();
    }

    public static UserRegisterForm normalize(UserRegisterForm request) {
        return UserRegisterForm.builder()
                .username(normalizeString(request.getUsername()))
                .email(normalizeString(request.getEmail()))
                .password(request.getPassword())
                .passwordConfirm(request.getPasswordConfirm())
                .build();
    }

    public static UserLoginForm normalize(UserLoginForm request) {
        return UserLoginForm.builder()
                .returnUrl(normalizeString(request.getReturnUrl()))
                .username(normalizeString(request.getUsername()))
                .password(normalizeString(request.getPassword()))
                .build();
    }
}
