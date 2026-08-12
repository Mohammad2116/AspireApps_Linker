package ir.aspireapps.linker.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotEmpty @Size(min = 3, max = 100)
        String username,

        @NotEmpty @Size(min = 5, max = 254)
        @Email
        String email,

        @NotEmpty @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,100}$",
                message = "Password must be 8-100 chars and include at least one letter, one number, and one special character."
        )
        String password
) {
}
