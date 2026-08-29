package ir.aspireapps.linker.userservice.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddLinkForm {
    @NotEmpty
    @Size(min = 1, max = 254)
    private String title;

    @NotEmpty
    @Size(min = 3, max = 1024)
    private String originalUrl;

    @NotNull
    private boolean status;
    @NotNull
    private LocalDate expiresAt;
}
