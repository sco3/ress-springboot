package sco.server.app;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
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

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Path.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
        Set<org.springframework.beans.factory.config.BeanDefinition> beans =
                scanner.findCandidateComponents("sco.server.provider");
        for (org.springframework.beans.factory.config.BeanDefinition bean : beans) {
            try {
                config.register(Class.forName(bean.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

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
