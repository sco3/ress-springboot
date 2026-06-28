package com.tnf.cas.webserver;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Stopwatch;

public class DevTestEncrypt implements Runnable {

    private KeyPair mPair;
    private PrivateKey mPriv;
    private PublicKey mPub;

    public static void main(String[] argv) throws Exception {
        DevTestEncrypt sign = new DevTestEncrypt();
        sign.init();

        sign.run(true);
        sign.run(true);
        sign.run(true);
        Stopwatch timer = Stopwatch.createStarted();

        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(8);
        int n = 50000;
        for (int i = 0; i < n; i++) {
            pool.execute(sign);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);

        double seconds = timer.elapsed(TimeUnit.SECONDS);
        System.out.println(seconds);
        System.out.println(n / seconds);
    }

    private void init() throws Exception {

        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
            for (String key : provider.stringPropertyNames()) {
                // if (key.toLowerCase().indexOf("sign") >= 0) {
                System.out
                        .println("\t" + key + "\t" + provider.getProperty(key));
                // }
            }
        }
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyGen.initialize(512, random);
        mPair = keyGen.generateKeyPair();
        mPriv = mPair.getPrivate();
        mPub = mPair.getPublic();
    }

    private int run(boolean print) throws Exception {
        SecretKey secret = KeyGenerator.getInstance("RC2").generateKey();
        byte[] enc = secret.getEncoded();

        Cipher dsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        dsa.init(Cipher.ENCRYPT_MODE, mPub);
        String msg0 = "" + Long.toHexString(System.currentTimeMillis())
                + "asdf:asdf:asdfasdfasdfasdfasdfasdfasdfadsfasdfasdf asdfasdfasdfasdfasdf";
        String msg = msg0.substring(0, 53);
        // String msg2 = msg0.substring(53);

        byte[] a = dsa.doFinal(msg.getBytes());
        Cipher.getInstance("RC4");

        if (print) {
            String str = DatatypeConverter.printBase64Binary(a);
            System.out.println(a.length + ":" + str + ":" + enc.length);

        }

        dsa.init(Cipher.DECRYPT_MODE, mPriv);
        String msg02 = new String(dsa.doFinal(a));
        if (!msg.equals(msg02)) {
            System.out.println("Sorry.");
            System.exit(0);

        }

        return a.length;
    }

    @Override
    public void run() {
        try {
            run(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
