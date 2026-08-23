package ir.aspireapps.linker.userservice.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GeneralBusinessException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String code;

    public GeneralBusinessException(String message, HttpStatus status, String code) {
        super(message);
        this.httpStatus = status;
        this.code = code;
    }
}
