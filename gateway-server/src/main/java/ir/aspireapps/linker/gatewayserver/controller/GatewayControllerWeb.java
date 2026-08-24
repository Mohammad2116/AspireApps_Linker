package ir.aspireapps.linker.gatewayserver.controller;

import org.springframework.http.HttpCookie;
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
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("REFRESH_TOKEN");
        if (cookie != null)
            model.addAttribute("AUTHENTICATED", true);
        else
            model.addAttribute("AUTHENTICATED", false);

        return "home";
    }
}
