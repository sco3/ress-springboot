package test.spring.non.scannable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.tnf.cas.db.DummyHistFinder;
import com.tnf.cas.db.HistoricalFinder;

import sco.security.Authenticator;
import sco.security.HmacAuthenticator;
import sco.security.KeyProvider;
import sco.security.PasswordFileLoginProvider;
import sco.security.SingleKeyProvider;

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
