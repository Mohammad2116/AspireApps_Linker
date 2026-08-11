package ir.aspireapps.linker.gatewayserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayTestController {
    @GetMapping("/ir/aspireapps/linker/api/v1/gateway/anything")
    public ResponseEntity<String> anything() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Gateway Test Success, gateway working correctly...");
    }

}
