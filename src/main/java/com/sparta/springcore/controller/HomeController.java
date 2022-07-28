package com.sparta.springcore.controller;

import com.sparta.springcore.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/") //동적 웹페이지를 구현할 때에는 Template engine에게 View,Model을 전달해서 구현해야하기 때문에 타임리프 default값에 의해 index.html이 templates 안에 있어야한다.
    public String home(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) { //@Authenti~ 어노테이션을 통해서 UserDetailsImpl을 Controller에서 받을 수 있다.
        model.addAttribute("username", userDetails.getUsername());
        return "index";
    }
}
