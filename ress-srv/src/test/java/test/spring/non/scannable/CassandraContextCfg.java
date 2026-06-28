package test.spring.non.scannable;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
        "classpath:embedded_cassandra_connection.properties",
        "file:src/main/assembly/cfg/cfg-cas.properties.template"
}, ignoreResourceNotFound = true)
@ComponentScan(basePackages = {"sco.server"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableAutoConfiguration.class))
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.class})
public class CassandraContextCfg {

}
