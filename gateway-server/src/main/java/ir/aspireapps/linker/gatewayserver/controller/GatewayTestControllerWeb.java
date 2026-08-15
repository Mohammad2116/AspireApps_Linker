package ir.aspireapps.linker.gatewayserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GatewayTestControllerWeb {
    @GetMapping("/ir/aspireapps/linker/gateway/web/v1/anything")
    public String anything() {
        return "test";
    }
}
