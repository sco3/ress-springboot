package test.spring.non.scannable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.tnf.cas.db.DummyHistFinder;
import com.tnf.cas.db.HistoricalFinder;
import com.tnf.cas.security.Authenticator;
import com.tnf.cas.security.HmacAuthenticator;
import com.tnf.cas.security.KeyProvider;
import com.tnf.cas.security.PasswordFileLoginProvider;
import com.tnf.cas.security.SingleKeyProvider;

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
