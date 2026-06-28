package test.spring.non.scannable;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ //
        "classpath:test-beans.xml" //
})
@ComponentScan(basePackages = { "com.tnf.cas" })

public class ContextCfg {

}
