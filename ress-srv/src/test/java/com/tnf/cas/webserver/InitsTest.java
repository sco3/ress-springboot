package com.tnf.cas.webserver;

import static org.junit.Assert.*;
import static sco.server.WebInitiator.scramble;

import java.util.regex.Pattern;

import org.junit.Test;

import sco.server.WebInitiator;

public class InitsTest {

    @Test
    public void test() {
        WebInitiator init = new WebInitiator();
        init.setKeystorePass("asdf1");
        init.setKeyPass("asdf2");
        String v = init.getValues();
        System.out.println(v);

        for (String var : v.split(Pattern.quote("\n"))) {
            String[] parts = var.split(Pattern.quote("="));
            if (parts[0].endsWith("Pass")) {
                String adsf = scramble(parts[1]);
                System.out.println(adsf);
                assertTrue(adsf.equals("asdf1") || adsf.equals("asdf2"));
            }
        }
    }

}
