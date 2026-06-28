package test.spring.non.scannable;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ //
        "file:src/main/assembly/cfg/beans.xml", //
        "classpath:test-cassandra-beans.xml" //
})
@ComponentScan(basePackages = { "com.tnf.cas" }, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableAutoConfiguration.class))
@EnableAutoConfiguration(exclude = { org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.class })

public class CassandraContextCfg {

}
