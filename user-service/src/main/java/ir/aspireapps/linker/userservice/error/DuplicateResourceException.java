package ir.aspireapps.linker.userservice.error;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends GeneralBusinessException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATED_RESOURCE");
    }
}
