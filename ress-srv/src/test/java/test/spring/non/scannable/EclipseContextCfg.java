package test.spring.non.scannable;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Configuration
@ImportResource({
        "file:src/main/assembly/cfg/beans.xml"
})
@ComponentScan(basePackages = {"com.tnf.cas", "sco.security"})
public class EclipseContextCfg {

    @Bean
    public PropertyPlaceholderConfigurer getCfg() {
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        FileSystemResource location1 = new FileSystemResource(
                "src/main/assembly/cfg/cfg-cas.properties.template"
        );
        ClassPathResource location2 = new ClassPathResource(
                "eclipse_cassandra_connection.properties"
        );
        cfg.setLocations(location1, location2);
        cfg.setIgnoreUnresolvablePlaceholders(true);
        return cfg;
    }
}
