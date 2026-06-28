package sco.server.app;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.SerializationFeature;

import sco.server.provider.StatusFilter;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    @Bean
    public ServletRegistrationBean jerseyServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                new ServletContainer(), "/*");
        registration.addInitParameter(
                ServerProperties.PROVIDER_PACKAGES,
                StatusFilter.class.getPackage().getName()
        );
        registration.addInitParameter(
                SerializationFeature.FAIL_ON_EMPTY_BEANS.toString(), "false"
        );
        registration.setLoadOnStartup(1);
        return registration;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
