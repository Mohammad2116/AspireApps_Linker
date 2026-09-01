package ir.aspireapps.linker.common.error;

import org.springframework.http.HttpStatus;

public class InvalidJwtToken extends GeneralBusinessException {
    public InvalidJwtToken(String message) {
        super(message, HttpStatus.FORBIDDEN, "INVALID_TOKEN");
    }
}
