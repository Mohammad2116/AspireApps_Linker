package ir.aspireapps.linker.gatewayserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;

@Controller
public class GatewayControllerWeb {
    @GetMapping("/ir/aspireapps/linker/home")
    public String home(
            Model model,
            ServerWebExchange exchange
    ) {
        boolean authenticated = Boolean.TRUE.equals(exchange.getAttribute("AUTHENTICATED"));
        if (authenticated)
            model.addAttribute("loggedIn", true);
        return "home";
    }
}
