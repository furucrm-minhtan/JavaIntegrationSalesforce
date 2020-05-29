package com.citron.javaintegrationsalesforce.services;

import com.citron.javaintegrationsalesforce.model.ApiConnect;
import org.springframework.web.servlet.view.RedirectView;

public interface AuthService {
    public abstract ApiConnect checkConnectSalesforce();
    public abstract RedirectView authenSalesforce();
    public abstract void getTokenSalesforce(String code);
}
