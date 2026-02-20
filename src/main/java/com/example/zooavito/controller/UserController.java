package com.example.zooavito.controller;

import com.example.zooavito.model.User;
import com.example.zooavito.service.SecurityService;
import com.example.zooavito.service.UserService;
import com.example.zooavito.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") User userForm,
                               BindingResult bindingResult, Model model) {

        // Проверка на существующий email
        if (userService.findByEmail(userForm.getEmail()) != null) {
            bindingResult.rejectValue("email", "DuplicateEmail");
        }

        userValidator.validate(userForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(userForm);

        // Сохраняем исходный пароль для автологина
        String rawPassword = userForm.getPassword();
        securityService.autoLogin(userForm.getEmail(), rawPassword);

        return "redirect:/welcome";
    }

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout) {
        if (error != null) {
            model.addAttribute("error", "Не правильный пароль или имя пользователя!");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        return "login";
    }

    @GetMapping({"/", "/welcome"})
    public String welcome(Model model) {
        return "welcome";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        return "admin";
    }
}