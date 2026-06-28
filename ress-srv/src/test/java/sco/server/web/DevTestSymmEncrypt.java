package sco.server.web;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Stopwatch;

public class DevTestSymmEncrypt implements Runnable {

    private SecretKey mSecret;

    public static void main(String[] argv) throws Exception {
        DevTestSymmEncrypt sign = new DevTestSymmEncrypt();
        sign.init();

        sign.run(true);
        sign.run(true);
        sign.run(true);
        Stopwatch timer = Stopwatch.createStarted();

        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(8);
        int n = 2250000;
        for (int i = 0; i < n; i++) {
            pool.execute(sign);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);

        double seconds = timer.elapsed(TimeUnit.SECONDS);
        System.out.println(seconds);
        System.out.println(n / seconds);
        System.out.println(n / seconds / 12500);
    }

    private void init() throws Exception {
        KeyGenerator instance = KeyGenerator.getInstance("AES");
        instance.init(256);
        mSecret = instance.generateKey();
        byte[] enc = mSecret.getEncoded();
        System.out.println("keylen:" + enc.length);
        Date d = new Date(Integer.MAX_VALUE * 60L * 1000L);
        System.out.println("date:" + d);

    }

    private int run(boolean print) throws Exception {

        Cipher dsa = Cipher.getInstance("AES/ECB/PKCS5Padding");

        dsa.init(Cipher.ENCRYPT_MODE, mSecret);
        String msg0 = "" + Long.toHexString(System.currentTimeMillis())
                + "asdf:asdfasdfasdfqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerqwerw";
        String msg = msg0.substring(0, 61);
        String msg2 = "" + msg0.substring(61);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(dsa.update(msg.getBytes()));
        if (msg2.length() > 0) {
            out.write(dsa.update(msg2.getBytes()));
        }
        out.write(dsa.doFinal());
        byte[] a = out.toByteArray();
        if (print) {
            System.out.println(dsa.getAlgorithm());
            {

                String str = DatatypeConverter
                        .printBase64Binary(out.toByteArray());
                System.out.println(a.length + ":" + str);
            }

        }

        dsa.init(Cipher.DECRYPT_MODE, mSecret);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        out2.write(dsa.update(a));
        out2.write(dsa.doFinal());
        String msg02 = new String(out2.toByteArray());
        if (!msg0.equals(msg02)) {
            System.out.println("Decode failed.");
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
