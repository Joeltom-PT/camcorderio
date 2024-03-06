package com.camcorderio.camcorderio.controller;

import com.camcorderio.camcorderio.exceptions.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String handleEmailAlreadyExistException(EmailAlreadyExistException ex, Model model) {
        model.addAttribute("emailError", ex.getMessage());
        return "register";
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public String handleInvalidPasswordException(InvalidPasswordException ex, Model model) {
        model.addAttribute("passwordError", ex.getMessage());
        return "register";
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public String handleInvalidUsernameException(InvalidUsernameException ex, Model model) {
        model.addAttribute("usernameError", ex.getMessage());
        return "register";
    }

    @ExceptionHandler(InvalidEmailException.class)
    public String handleInvalidEmailException(InvalidEmailException ex, Model model) {
        model.addAttribute("emailError", ex.getMessage());
        return "register";
    }

    @ExceptionHandler(UserDisabledException.class)
    public void handleDisabledException(UserDisabledException e ,HttpSession session){
        session.setAttribute("userDisabled",e);
    }
}