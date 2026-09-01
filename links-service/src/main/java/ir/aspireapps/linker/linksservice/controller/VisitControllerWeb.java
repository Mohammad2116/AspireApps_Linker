package ir.aspireapps.linker.linksservice.controller;

import ir.aspireapps.linker.linksservice.dto.RedirectResponse;
import ir.aspireapps.linker.linksservice.service.RedirectService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class VisitControllerWeb {
    private final RedirectService redirectService;

    @GetMapping("/ir/aspireapps/linker/visit/{shorted}")
    public String visitWeb(
            @NotBlank @PathVariable String shorted,
            Model model,
            HttpServletRequest request) {

        RedirectResponse result;
        try {
            result = redirectService.visitApi(shorted);
            model.addAttribute("target", result.originalUrl());
            log.info("result: {}", result);
            return "redirect:" + result.originalUrl();
        } catch (RuntimeException e) {
            if (request.getCookies() != null) {
                List<Cookie> cookies = Arrays.stream(request.getCookies()).toList();
                if (!cookies.isEmpty()) {
                    boolean authData = cookies.stream().anyMatch(
                            (cookie) -> cookie.getName().equals("REFRESH_TOKEN"));
                    model.addAttribute("AUTHENTICATED", authData);
                }
            }
            model.addAttribute("redirectError", "Invalid Shorted Link");
            return "home";
        }
    }
}
