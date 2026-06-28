package test.spring.non.scannable;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TestMockBeansConfig.class)
@ComponentScan(basePackages = {"com.tnf.cas", "sco.provider", "sco.server", "sco.security"})
public class ContextCfg {

}
