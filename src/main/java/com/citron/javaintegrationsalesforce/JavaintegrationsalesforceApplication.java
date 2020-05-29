package com.citron.javaintegrationsalesforce;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

@SpringBootApplication
@EnableJpaRepositories("com.citron.javaintegrationsalesforce.repository")
@EnableConfigurationProperties(ConnectSalesforceConfig.class)
public class JavaintegrationsalesforceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaintegrationsalesforceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
