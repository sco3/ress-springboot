package test.spring.non.scannable;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.tnf.cas.webserver.WebServer;

@Configuration
public class WebTestCfg {

    @Bean
    WebServer getWebServer() {
        return new WebServer();
    }

    @Bean
    PropertyPlaceholderConfigurer getCfg() {
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        FileSystemResource location = new FileSystemResource(//
                "src/main/assembly/cfg/cfg-cas.properties.template"//
        );
        cfg.setLocation(location);
        cfg.setIgnoreUnresolvablePlaceholders(true);
        return cfg;
    }
}
