package ir.aspireapps.linker.common.error;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends GeneralBusinessException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
