package ir.aspireapps.linker.gatewayserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GatewayTestControllerWeb {
    @GetMapping("/ir/aspireapps/web/v1/linker/gateway/anything")
    public String anything() {
        return "test";
    }
}
