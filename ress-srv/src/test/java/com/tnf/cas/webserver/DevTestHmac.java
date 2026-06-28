package com.tnf.cas.webserver;

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import com.google.common.base.Stopwatch;

public class DevTestHmac {

    private static final String HMAC = "HmacSHA256";

    @Test
    public void test() throws Exception {
        String message = "";
        for (int i = 0; i < 3; i++) {
            message += ":" + new Date();
        }

        byte[] bytes = new byte[64];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(bytes);

        Stopwatch timer = Stopwatch.createStarted();
        int loops = 1000;

        {
            for (int i = 0; i < loops; i++) {
                SecretKeySpec key = new SecretKeySpec(bytes, HMAC);
                Mac mac = Mac.getInstance(HMAC);
                mac.init(key);
                mac.update(message.getBytes());
                byte[] sign = mac.doFinal();
                String r = encodeBase64URLSafeString(sign);
                if (i < 3) {
                    System.out.println(r);
                }
            }
            double seconds = timer.elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            System.out.println(
                    HMAC + " " + loops + " tests duration (sec):" + seconds//
            );
            System.out.println(//
                    HMAC + " " + "Ops per second: " + loops / seconds//
            );
            System.out.println(//
                    HMAC + " " + "Op time (sec): " + seconds / loops//
            );
        }
        {
            SecretKeySpec key = new SecretKeySpec(bytes, HMAC);
            for (int i = 0; i < loops; i++) {
                Mac mac = Mac.getInstance(HMAC);
                mac.init(key);
                mac.update(message.getBytes());
                byte[] sign = mac.doFinal();
                String r = encodeBase64URLSafeString(sign);
                if (i < 3) {
                    System.out.println(r);
                }
            }

            double seconds = timer.elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            System.out.println(
                    HMAC + " " + loops + " tests duration (sec):" + seconds//
            );
            System.out.println(//
                    HMAC + " " + "Ops per second: " + loops / seconds//
            );
            System.out.println(//
                    HMAC + " " + "Op time (sec): " + seconds / loops//
            );

        }

    }

}
