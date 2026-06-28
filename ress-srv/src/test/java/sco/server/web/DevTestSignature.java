package sco.server.web;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Stopwatch;

public class DevTestSignature implements Runnable {

    private KeyPair mPair;
    private PrivateKey mPriv;
    private PublicKey mPub;

    public static void main(String[] argv) throws Exception {
        DevTestSignature sign = new DevTestSignature();
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

        Signature dsa = Signature.getInstance("SHA1withRSA");
        dsa.initSign(mPriv);
        String msg = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf"
                + System.currentTimeMillis();
        dsa.update(msg.getBytes());
        byte[] a = dsa.sign();
        if (print) {
            System.out.println(DatatypeConverter.printBase64Binary(a));
        }

        dsa.initVerify(mPub);

        dsa.update(msg.getBytes());
        dsa.verify(a);
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
