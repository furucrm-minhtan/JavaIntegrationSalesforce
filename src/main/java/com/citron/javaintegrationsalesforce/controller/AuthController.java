package com.citron.javaintegrationsalesforce.controller;

import com.citron.javaintegrationsalesforce.services.AuthServiceImp;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

@Controller
public class AuthController {
    @Autowired
    private AuthServiceImp authServiceImp;

    public AuthController(AuthServiceImp _authServiceImp) {
        this.authServiceImp = _authServiceImp;
    }
	
	@GetMapping("/login")
    public String index(Principal principal) {
        return principal == null ?  "Auth/login" : "redirect:/proposal";
    }
	
    @RequestMapping(path = "/profile", method = RequestMethod.GET)
    public String profile(Model model) throws JSONException {
        model.addAttribute("api", authServiceImp.checkConnectSalesforce() );
        return "user/profile";
    }

    @RequestMapping(path = "/oauth2/connectSalesforce", method = RequestMethod.GET)
    public RedirectView authSalesforce() {
        return authServiceImp.authenSalesforce();
    }

    @RequestMapping(path = "/oauth2/callback", method = RequestMethod.GET)
    public String callBack(@RequestParam("code") String code) {
        authServiceImp.getTokenSalesforce(code);
        return "redirect:/profile";
    }
}
