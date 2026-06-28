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
import sco.server.provider.BadParameters;
import sco.server.provider.Compressor;
import sco.server.provider.CorsFilter;
import sco.server.provider.Delay;
import sco.server.provider.DelayAsync;
import sco.server.provider.DummyHistSchemaGenerator;
import sco.server.provider.ExceptionHandler;
import sco.server.provider.HistSchemaGeneratorRest;
import sco.server.provider.ImsiRest;
import sco.server.provider.Informer;
import sco.server.provider.InformerRecord;
import sco.server.provider.Login;
import sco.server.provider.NoDataFound;
import sco.server.provider.NotFound404;
import sco.server.provider.RolesFeatureActivator;
import sco.server.provider.Search;
import sco.server.provider.StatusFilter;
import sco.server.provider.SubProfilerRest;
import sco.server.provider.SubProfileGeneratorRest;
import sco.server.provider.SwitchSetter;
import sco.server.provider.TokenValidator;

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
        config.register(BadParameters.class);
        config.register(Compressor.class);
        config.register(CorsFilter.class);
        config.register(Delay.class);
        config.register(DelayAsync.class);
        config.register(DummyHistSchemaGenerator.class);
        config.register(ExceptionHandler.class);
        config.register(HistSchemaGeneratorRest.class);
        config.register(ImsiRest.class);
        config.register(Informer.class);
        config.register(InformerRecord.class);
        config.register(Login.class);
        config.register(NoDataFound.class);
        config.register(NotFound404.class);
        config.register(RolesFeatureActivator.class);
        config.register(Search.class);
        config.register(StatusFilter.class);
        config.register(SubProfilerRest.class);
        config.register(SubProfileGeneratorRest.class);
        config.register(SwitchSetter.class);
        config.register(TokenValidator.class);
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
