package com.citron.javaintegrationsalesforce.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handlerError(HttpServletRequest request) {
        return "error/error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
