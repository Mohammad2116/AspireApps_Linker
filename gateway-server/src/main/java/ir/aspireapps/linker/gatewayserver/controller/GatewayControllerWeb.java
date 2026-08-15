package ir.aspireapps.linker.gatewayserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GatewayControllerWeb {
    @GetMapping("/ir/aspireapps/linker/home")
    public String home() {
        return "home";
    }
}
