package com.camcorderio.camcorderio.controller.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request , Model model){

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        String errorPage = "error";
        String errorDescription = "";

        if (status!=null){
            int statusCode = Integer.parseInt(status.toString());

            switch (statusCode) {
                case 404:
                    errorDescription = "The requested resource was not found";
                    break;
                case 403:
                    errorDescription = "Access to the requested resource is forbidden";
                    break;
                case 500:
                    errorDescription = "An internal server error occurred";

            }
        }

        model.addAttribute("status", status);
        model.addAttribute("errorDescription", errorDescription);

        return errorPage;
    }
}