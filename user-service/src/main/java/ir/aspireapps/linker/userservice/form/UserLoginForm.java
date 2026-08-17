package ir.aspireapps.linker.userservice.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginForm {
    private String returnUrl;

    @NotEmpty
    @Size(min = 3, max = 100)
    private String username;

    @NotEmpty
    @Size(min = 8, max = 100)
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,100}$",
            message = "Password must be 8-100 chars and include at least one letter, one number, and one special character."
    )
    private String password;
}
