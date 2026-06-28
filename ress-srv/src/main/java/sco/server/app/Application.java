package sco.server.app;

import javax.annotation.PostConstruct;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.SerializationFeature;

import sco.common.util.CasPropertiesHelper;

@SpringBootApplication(scanBasePackages = "sco.server")
public class Application extends SpringBootServletInitializer {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        CasPropertiesHelper.setEnvironment(environment);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    @Bean
    public ServletRegistrationBean jerseyServlet() {
        ResourceConfig config = new ResourceConfig();
        config.packages(true, "sco.server.provider");
        config.property(SerializationFeature.FAIL_ON_EMPTY_BEANS.toString(), false);

        ServletRegistrationBean registration = new ServletRegistrationBean(
                new ServletContainer(config), "/*");
        registration.setLoadOnStartup(1);
        return registration;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
