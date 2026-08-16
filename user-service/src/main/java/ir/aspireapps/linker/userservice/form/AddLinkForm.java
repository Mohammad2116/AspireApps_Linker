package ir.aspireapps.linker.userservice.form;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AddLinkForm {
    private String title;
    private String originalUrl;
    private boolean status;
    private LocalDate expiresAt;
}
