package com.m.m.RealTimeChat.Controllers;

import com.m.m.RealTimeChat.Services.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final AdminService adminService;

    public LoginController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/login-error")
    public String getLoginPage(HttpServletRequest request, Model model) {

        model.addAttribute("message", adminService.manageErrorMessage(request));
        return "loginPage";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "loginPage";
    }
}
