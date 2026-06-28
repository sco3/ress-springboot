package com.tnf.cas.security;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

import java.security.Principal;
import java.util.regex.Pattern;

import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Charsets;
import com.tnf.cas.web.WebServerConstants;

public class PasswordFileLoginProvider
        implements LoginProvider, WebServerConstants {

    private Logger mTrace = LoggerFactory
            .getLogger(PasswordFileLoginProvider.class);

    private KeyProvider mKeyProvider;

    private int mMinutesToLive;

    private PropsFileUserStorage mUsers = new PropsFileUserStorage();

    @Value("${" + TOKEN_MINUTES_TO_LIVE + "}")
    public void setMinutesToLive(int minutesToLive) {
        mMinutesToLive = minutesToLive;
    }

    public int getMinutesToLive() {
        return mMinutesToLive;
    }

    public PasswordFileLoginProvider() {
        mTrace.info("Ready.");
    }

    @Override
    public String login(String user, String password) {
        String result = null;
        try {
            if (user != null && password != null //
                    && mUsers.isValid(user, password) //
            ) {

                result = b64enc(user);
                long timeLimit = System.currentTimeMillis() / MINUTE
                        + getMinutesToLive() + 1;
                result += ":" + Long.toString(timeLimit, Character.MAX_RADIX);
                if (getKeyProvider() != null) {
                    result = signToken(result);
                }
            }
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
        return result;
    }

    private String b64enc(String msg) {
        byte[] bytes = msg.getBytes(Charsets.UTF_8);
        return encodeBase64URLSafeString(bytes);
    }

    private String b64dec(String msg) {
        byte[] dec = decodeBase64(msg.getBytes(Charsets.UTF_8));
        return new String(dec, Charsets.UTF_8);
    }

    private String b64enc(byte[] bytes) {
        return encodeBase64URLSafeString(bytes);
    }

    private String getSign(byte[] secret, String message) {
        String sign = null;
        try {
            byte[] msgBytes = message.getBytes(Charsets.UTF_8);
            Authenticator auth = mKeyProvider.getAuthenticator();
            byte[] dgst = auth.sign(msgBytes, secret);
            sign = b64enc(dgst);
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
        return sign;
    }

    private String signToken(String message) {
        KeyRecord secReco = getKeyProvider().getCurrentKey();
        String sign = getSign(secReco.getSecret(), message);
        sign = secReco.getIndex() + ":" + message + ":" + sign;
        return sign;
    }

    @Override
    public SecurityContext validate(String token) {
        SecurityContext result = null;
        try {
            String[] parts = token.split(Pattern.quote(":"));
            if (parts != null && parts.length >= 4) {
                String keyId = parts[0];
                String user = parts[1];
                String time = parts[2];
                String tokenSign = parts[3];
                String sign = getSign(getKeyProvider().getKey(keyId),
                        user + ":" + time);
                long timeLimit = Long.parseLong(time, Character.MAX_RADIX);
                long currentMinute = System.currentTimeMillis() / MINUTE;
                if (sign.equals(tokenSign) && currentMinute <= timeLimit) {
                    final String name = b64dec(parts[1]);
                    final Principal principal = new Principal() {
                        @Override
                        public String getName() {
                            return name;
                        }
                    };

                    result = new SecurityContext() {
                        @Override
                        public Principal getUserPrincipal() {
                            return principal;
                        }

                        @Override
                        public boolean isUserInRole(final String role) {
                            return HRCC_ROLE.equals(role);
                        }

                        @Override
                        public boolean isSecure() {
                            return true;
                        }

                        @Override
                        public String getAuthenticationScheme() {
                            return null;
                        }

                        public String toString() {
                            return principal.toString();
                        }
                    };
                }
            }
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
        return result;
    }

    public KeyProvider getKeyProvider() {
        return mKeyProvider;
    }

    public void setKeyProvider(KeyProvider keyProvider) {
        mKeyProvider = keyProvider;
    }

}
