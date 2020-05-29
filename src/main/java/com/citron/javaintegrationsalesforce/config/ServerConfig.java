package com.citron.javaintegrationsalesforce.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServerConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Value("${http.port}")
    private int httpPort;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if(factory instanceof TomcatServletWebServerFactory) {
            TomcatServletWebServerFactory tomcatFactory = (TomcatServletWebServerFactory) factory;
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setPort(httpPort);
            tomcatFactory.addAdditionalTomcatConnectors(connector);
        }
    }
}
