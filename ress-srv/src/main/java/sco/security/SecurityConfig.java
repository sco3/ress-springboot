package sco.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public HmacAuthenticator authenticator() {
        return new HmacAuthenticator();
    }

    @Bean
    public SingleKeyProvider keyProvider(HmacAuthenticator authenticator) {
        SingleKeyProvider keyProvider = new SingleKeyProvider();
        keyProvider.setAuthenticator(authenticator);
        return keyProvider;
    }

    @Bean
    public PasswordFileLoginProvider loginProvider(SingleKeyProvider keyProvider) {
        PasswordFileLoginProvider loginProvider = new PasswordFileLoginProvider();
        loginProvider.setKeyProvider(keyProvider);
        return loginProvider;
    }
}
