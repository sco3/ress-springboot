package test.spring.non.scannable;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TestMockBeansConfig.class)
@ComponentScan(basePackages = {"sco.server"})
public class ContextCfg {

}
