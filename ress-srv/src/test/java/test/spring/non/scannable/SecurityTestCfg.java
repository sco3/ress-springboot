package test.spring.non.scannable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import sco.server.db.DummyHistFinder;
import sco.server.db.HistoricalFinder;
import sco.server.security.Authenticator;
import sco.server.security.HmacAuthenticator;
import sco.server.security.KeyProvider;
import sco.server.security.PasswordFileLoginProvider;
import sco.server.security.SingleKeyProvider;

@Configuration
@PropertySource("classpath:webroot.properties")
public class SecurityTestCfg {

	@Bean
	public Authenticator authenticator() {
		return new HmacAuthenticator();
	}

	@Bean
	public KeyProvider keyProvider() {
		SingleKeyProvider kp = new SingleKeyProvider();
		kp.setAuthenticator(authenticator());
		return kp;
	}

	@Bean
	public PasswordFileLoginProvider loginProvider() {
		PasswordFileLoginProvider p = new PasswordFileLoginProvider();
		p.setKeyProvider(keyProvider());
		return p;
	}

	@Bean
	public HistoricalFinder histFinder() {
		return new DummyHistFinder();
	}

}
