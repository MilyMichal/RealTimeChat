package com.m.m.RealTimeChat.Controllers;




import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/sessionError")
public class SessionErrorContoller {

    @GetMapping
    public String showSessionError(){
         return "session";
    }


}
