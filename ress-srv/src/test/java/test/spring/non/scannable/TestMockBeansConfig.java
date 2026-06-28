package test.spring.non.scannable;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.datastax.driver.core.Session;

import sco.server.db.DummyHistFinder;
import sco.server.db.DummyImsiResolver;
import sco.server.db.DummySubProfileGenerator;
import sco.server.db.DummySubProfiler;
import sco.server.db.HistoricalFinder;
import sco.server.db.HistoricalSchemaGenerator;
import sco.server.db.ImsiResolver;
import sco.server.db.ProfileSchemaGenerator;
import sco.server.db.SubProfiler;
import sco.server.provider.DummyHistSchemaGenerator;

@Configuration
@PropertySource({
        "classpath:embedded_cassandra_connection.properties",
        "classpath:webroot.properties"
})
public class TestMockBeansConfig {

    @Bean("testString")
    public String testString() {
        return "test-beans.xml";
    }

    @Bean
    @Primary
    public Session session() {
        return mock(Session.class);
    }

    @Bean
    @Primary
    public ImsiResolver imsiResolver() {
        return new DummyImsiResolver();
    }

    @Bean
    @Primary
    public SubProfiler subProfiler() {
        return new DummySubProfiler();
    }

    @Bean
    @Primary
    public ProfileSchemaGenerator subProfileGenerator() {
        return new DummySubProfileGenerator();
    }

    @Bean
    @Primary
    public HistoricalFinder histFinder() {
        return new DummyHistFinder();
    }

    @Bean
    @Primary
    public HistoricalSchemaGenerator histSchemaGenerator() {
        return new DummyHistSchemaGenerator();
    }
}
