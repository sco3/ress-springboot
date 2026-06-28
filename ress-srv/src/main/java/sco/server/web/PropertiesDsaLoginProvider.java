package sco.server.web;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Calendar;
import java.util.regex.Pattern;

import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sco.server.security.LoginProvider;
import sco.server.security.PropsFileUserStorage;

public class PropertiesDsaLoginProvider
        implements LoginProvider, WebServerConstants {
    Logger mTrace = LoggerFactory.getLogger(PropertiesDsaLoginProvider.class);
    private PropsFileUserStorage mUsers = new PropsFileUserStorage();

    private PrivateKey mPriv;
    private PublicKey mPub;

    PrivateKey getPrivate() {
        if (mPriv == null) {
            genKey();
        }
        return mPriv;
    }

    PublicKey getPublic() {
        if (mPub == null) {
            genKey();
        }
        return mPub;
    }

    void genKey() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(128, random);
            KeyPair key = keyGen.generateKeyPair();
            mPriv = key.getPrivate();
            mPub = key.getPublic();
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
    }

    @Override
    public String login(String name, String password) {
        String result = null;
        if (mUsers.isValid(name, password)) {
            try {
                Signature dsa = Signature.getInstance("SHA1withDSA");
                dsa.initSign(getPrivate());
                String msg = DatatypeConverter
                        .printBase64Binary(name.getBytes());

                long expires = Calendar.getInstance().getTimeInMillis()
                        + 24 * 3600000;
                msg += ":" + expires;
                dsa.update(msg.getBytes());
                byte[] a = dsa.sign();

                msg += "?" + DatatypeConverter.printBase64Binary(a);
            } catch (Exception e) {
                mTrace.error("{}", e);
            }
        }
        return result;
    }

    @Override
    public SecurityContext validate(String token) {
        SecurityContext result = null;
        try {
            String[] parts = token.split(Pattern.quote("?"));
            String msg = parts[0];
            byte[] sign = DatatypeConverter.parseBase64Binary(parts[1]);
            Signature dsa = Signature.getInstance("SHA1withDSA");
            dsa.initVerify(getPublic());
            dsa.update(msg.getBytes());
            boolean ok = dsa.verify(sign);
            if (ok) {
                // TODO dz extract name from token or delete class at all
                final String name = msg;
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
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
        return result;
    }
}
