package com.tnf.cas.webserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tnf.cas.db.DbHistoricalFinder;
import com.tnf.cas.db.DummyHistFinder;
import com.tnf.cas.db.DummyImsiResolver;
import com.tnf.cas.db.DummySubProfileGenerator;
import com.tnf.cas.db.DummySubProfiler;
import com.tnf.cas.db.HistoricalSchemaGenerator;
import com.tnf.cas.db.Switcher;
import com.tnf.cas.provider.DummyHistSchemaGenerator;
import com.tnf.cas.provider.HistSchemaGeneratorRest;
import com.tnf.cas.provider.ImsiRest;
import com.tnf.cas.provider.Search;
import com.tnf.cas.provider.SubProfileGeneratorRest;
import com.tnf.cas.provider.SubProfilerRest;
import com.tnf.cas.web.WebServerConstants;

import sco.server.WebServer;
import test.spring.non.scannable.ContextCfg;
import test.spring.non.scannable.WebTestCfg;

public class WebServicesTest implements WebServerConstants {

    private static final Logger mTrace = LoggerFactory.getLogger(WebServicesTest.class);

    private static WebServer mWebServer;
    private static int mPort;

    private static WebApplicationContext mBeans;

    private DecompressingHttpClient hc;

    private DefaultHttpClient backend;

    @BeforeClass
    public static void startup() throws Exception {
        @SuppressWarnings("resource")
        ApplicationContext ctx = new AnnotationConfigApplicationContext(//
                WebTestCfg.class//
        );
        for (String name : ctx.getBeanDefinitionNames()) {
            System.out.println(name);
        }
        mWebServer = ctx.getBean(WebServer.class);
        assertNotNull(mWebServer);
        mWebServer.setRandomPorts(true);
        mWebServer.setBeans(ContextCfg.class.getName());
        mWebServer.init();
        mBeans = mWebServer.getAppCtx();
        if (null == mBeans) {
            fail("Spring application context is invalid. Stop.");
        } else {
            mTrace.info("*** Spring application context loaded. Run. ***");
        }

        System.out.println(mWebServer.getOpenedSecurePort());
        mPort = mWebServer.getOpenedPort();
        System.out.println(mPort);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        mWebServer.shutdown();
    }

    public String getUrl(String url) {
        return String.format(url, mPort);
    }

    @Before
    public void setup() {
        Switcher switcher = mBeans.getBean(Switcher.class);
        switcher.setCompress(false);
        backend = new DefaultHttpClient();
        hc = new DecompressingHttpClient(backend);

    }

    @Test
    public void testUnprotected() throws Exception {

        HttpGet httpget = new HttpGet(
                getUrl("http://localhost:%s/" + REST_V1_PATH + "/testUnprotected"));
        HttpResponse response = hc.execute(httpget);
        HttpEntity entity = response.getEntity();

        JsonNode o = new ObjectMapper().readTree(entity.getContent());
        System.out.println(o);
        assertEquals("test-beans.xml", o.get("token").asText());
    }

    @Test
    public void test500() throws Exception {

        HttpGet httpget = new HttpGet(
                getUrl("http://localhost:%s/" + REST_V1_PATH + "/testException"));
        HttpResponse response = hc.execute(httpget);
        HttpEntity entity = response.getEntity();

        JsonNode o = new ObjectMapper().readTree(entity.getContent());
        System.out.println(o);
        assertEquals(500, o.get("status").asInt());
        assertEquals("Internal Error", o.get("message").asText());
    }

    @Test
    public void testProtected() throws Exception {
        { // no login no fun
            HttpGet httpget = new HttpGet(
                    getUrl("http://localhost:%s/" + REST_V1_PATH + "/testProtected"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(403, o.get("status").asInt());
            assertEquals("Forbidden", o.get("message").asText());
        }
        Cookie c = null;
        { // login
            HttpGet loginGet = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + "/login?user=tnf&password=tnf")//
            );

            HttpResponse response = hc.execute(loginGet);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertNotNull(o.get("token").asText());
            Header h = response.getFirstHeader("Set-Cookie");
            System.out.println(h);
            assertTrue(h.getValue().contains("token="));
            c = backend.getCookieStore().getCookies().get(0);
            assertNotNull(c);
            System.out.println(c);
            assertEquals("token", c.getName());
            assertNotNull(c.getValue());
        }
        {// protected resource with token cookie should work

            HttpGet httpget = new HttpGet(
                    getUrl("http://localhost:%s/" + REST_V1_PATH + "/testProtected"));
            BasicClientCookie cc = new BasicClientCookie( //
                    c.getName(), c.getValue() //
            );
            // apache http client does not send without domain
            cc.setDomain("localhost");
            backend.getCookieStore().clear();
            backend.getCookieStore().addCookie(cc);

            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());

            System.out.println(cc);
            System.out.println(o);

            assertNotNull(o.get("msg").asText());
        }
    }

    private void login() throws Exception { // login
        HttpGet loginGet = new HttpGet(getUrl(
                "http://localhost:%s/" + REST_V1_PATH + "/login?user=tnf&password=tnf")//
        );
        EntityUtils.consume(hc.execute(loginGet).getEntity());
        Cookie c = backend.getCookieStore().getCookies().get(0);
        System.out.println(c);
        BasicClientCookie cc = new BasicClientCookie( //
                c.getName(), c.getValue() //
        );
        // apache http client does not send without domain
        cc.setDomain("localhost");
        backend.getCookieStore().clear();
        backend.getCookieStore().addCookie(cc);
    }

    @Test
    public void testImsi() throws Exception {
        ImsiRest rest = mBeans.getBean(ImsiRest.class);
        assertNotNull(rest);
        assertNotNull(rest.getResolver());

        { // no login no fun
            backend.getCookieStore().clear();
            HttpGet httpget = new HttpGet(
                    getUrl("http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + IMSI));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(403, o.get("status").asInt());
            assertEquals("Forbidden", o.get("message").asText());
        }

        login();

        {
            HttpGet httpget = new HttpGet(
                    getUrl("http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + IMSI));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(0, o.size());
        }

        rest.setResolver(new DummyImsiResolver());

        { // no params
            HttpGet httpget = new HttpGet(
                    getUrl("http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + IMSI));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(0, o.size());
        }

        { // not found
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + IMSI + "?msisdn=asdf"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(0, o.size());
        }

        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + IMSI + "?msisdn=m1"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(1, o.size());
            assertEquals("i1-1", o.get(0).asText());
        }

        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + IMSI + "?msisdn=m1"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(1, o.size());
            assertEquals("i1-1", o.get(0).asText());
        }

        { // one good one not found - same results
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + IMSI + "?msisdn=m1&msisdn=asdf"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(1, o.size());
            assertEquals("i1-1", o.get(0).asText());
        }

        { // one param two results
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + IMSI + "?msisdn=m2"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(2, o.size());
            assertEquals("i2-1", o.get(0).asText());
            assertEquals("i2-2", o.get(1).asText());
        }

        { // two valid params
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + IMSI + "?msisdn=m1&msisdn=m3"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            ObjectMapper om = new ObjectMapper();
            ArrayList<?> a = om.readValue(o.toString(), ArrayList.class);
            assertEquals(4, o.size());
            assertTrue(a.contains("i1-1"));
            assertTrue(a.contains("i3-1"));
            assertTrue(a.contains("i3-2"));
            assertTrue(a.contains("i3-3"));
        }
    }

    @Test
    public void test404() throws Exception {
        HttpGet httpget = new HttpGet(
                getUrl("http://localhost:%s/" + REST_V1_PATH + "/blahblah"));
        HttpResponse response = hc.execute(httpget);
        HttpEntity entity = response.getEntity();

        JsonNode o = new ObjectMapper().readTree(entity.getContent());
        System.out.println(o);
        assertEquals(404, o.get("status").asInt());
        assertEquals("Not found", o.get("message").asText());
    }

    @Test
    public void test404g() throws Exception {
        HttpGet httpget = new HttpGet(
                getUrl("http://localhost:%s/" + REST_V1_PATH + "/blahblah?gzip"));
        HttpResponse response = hc.execute(httpget);
        HttpEntity entity = response.getEntity();

        JsonNode o = new ObjectMapper().readTree(entity.getContent());
        System.out.println(o);
        assertEquals(404, o.get("status").asInt());
        assertEquals("Not found", o.get("message").asText());
    }

    @Test
    public void testProfiler() throws Exception {
        SubProfilerRest rest = mBeans.getBean(SubProfilerRest.class);
        assertNotNull(rest);
        assertNotNull(rest.getProfiler());
        assertTrue(rest.getProfiler() instanceof DummySubProfiler);

        { // no login no fun
            backend.getCookieStore().clear();
            HttpGet httpget = new HttpGet(getUrl(
                    "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + PROFILE));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(403, o.get("status").asInt());
            assertEquals("Forbidden", o.get("message").asText());
        }
        login();
        {
            HttpGet httpget = new HttpGet(getUrl(
                    "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + PROFILE));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(0, o.size());
        }

        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + PROFILE + "?imsi=1"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(1, o.size());
            assertEquals("1", o.get(0).get("imsi").asText());
        }
        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + PROFILE + "?imsi=1&imsi=2"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(2, o.size());
            HashSet<String> set = new HashSet<String>();
            for (Iterator<JsonNode> i = o.elements(); i.hasNext();) {
                JsonNode node = i.next();
                set.add(node.get("imsi").asText());
            }
            assertTrue(set.contains("1"));
            assertTrue(set.contains("2"));
        }
    }

    @Test
    public void testProfileSchema() throws Exception {
        SubProfileGeneratorRest rest = mBeans.getBean(SubProfileGeneratorRest.class);
        assertNotNull(rest);
        assertNotNull(rest.getSchemaGenerator());
        assertTrue(rest.getSchemaGenerator() instanceof DummySubProfileGenerator);

        { // no login no fun
            backend.getCookieStore().clear();
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + PROFILE + DEF));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(403, o.get("status").asInt());
            assertEquals("Forbidden", o.get("message").asText());
        }
        login();
        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + PROFILE + DEF));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertNotNull(o);
            JsonNode props = o.path("items").path("properties");
            assertEquals(5, props.size());

            String exp = ("" //
                    + "{'imsi':{'type':'string'}," //
                    + "'address':{'type':'string'},"//
                    + "'age':{'type':'string'},"//
                    + "'current_plan':{'type':'string'},"//
                    + "'dummy':{'type':'string'}}"//
            );
            assertEquals(exp.replace('\'', '"'), props.toString());

            JsonNode reqs = o.path("items").path("required");
            assertEquals(5, reqs.size());

            exp = "['imsi','address','age','current_plan','dummy']";
            assertEquals(exp.replace('\'', '"'), reqs.toString());

        }
        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + PROFILE + DEF + "?something"));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertNotNull(o);
            JsonNode props = o.path("items").path("properties");
            assertEquals(5, props.size());

            String exp = ("" //
                    + "{'imsi':{'type':'string'}," //
                    + "'address':{'type':'string'},"//
                    + "'age':{'type':'string'},"//
                    + "'current_plan':{'type':'string'},"//
                    + "'dummy':{'type':'string'}}"//
            );
            assertEquals(exp.replace('\'', '"'), props.toString());

            JsonNode reqs = o.path("items").path("required");
            assertEquals(5, reqs.size());

            exp = "['imsi','address','age','current_plan','dummy']";
            assertEquals(exp.replace('\'', '"'), reqs.toString());

        }
    }

    @Test
    public void testHistoricalSchema() throws Exception {
        HistSchemaGeneratorRest rest = mBeans.getBean(HistSchemaGeneratorRest.class);
        assertNotNull(rest);
        HistoricalSchemaGenerator sGen = rest.getSchemaGenerator();
        assertNotNull(sGen);
        assertTrue(sGen instanceof DummyHistSchemaGenerator);

        { // no login no fun
            backend.getCookieStore().clear();
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + HISTDATA + DEF));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(403, o.get("status").asInt());
            assertEquals("Forbidden", o.get("message").asText());
        }
        login();
        {
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + HISTDATA + DEF));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertNotNull(o);
            JsonNode props = o.path("items").path("properties");
            assertEquals(2, props.size());

            String exp = ("" //
                    + "{'imsi':{'type':'string'}," //
                    + "'msisdn':{'type':'string'}"//
                    + "}"//
            );
            assertEquals(exp.replace('\'', '"'), props.toString());

            JsonNode reqs = o.path("items").path("required");
            assertEquals(2, reqs.size());

            exp = "['imsi','msisdn']";
            assertEquals(exp.replace('\'', '"'), reqs.toString());

        }
    }

    @Test
    public void testHistoricalData() throws Exception {
        Search rest = mBeans.getBean(Search.class);
        assertNotNull(rest);

        assertTrue(rest.getFinder() instanceof DummyHistFinder);

        { // no login no fun
            backend.getCookieStore().clear();
            HttpGet httpget = new HttpGet(getUrl("http://localhost:%s/" + REST_V1_PATH
                    + SUBSCRIBER + HISTDATA + ""));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertEquals(403, o.get("status").asInt());
            assertEquals("Forbidden", o.get("message").asText());
        }
        login();
        {
            HttpGet httpget = new HttpGet(getUrl("" //
                    + "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + HISTDATA + "" //
            ));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertNotNull(o);
            assertEquals(400, response.getStatusLine().getStatusCode());
            assertEquals(DbHistoricalFinder.MISSING_PARAMS, o.get("message").asText());
            assertEquals(400, o.get("status").asInt());
        }

        {
            HttpGet httpget = new HttpGet(getUrl("" //
                    + "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + HISTDATA //
                    + "?msisdn=1" //
            ));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            JsonNode o = new ObjectMapper().readTree(entity.getContent());
            System.out.println(o);
            assertNotNull(o);
            assertEquals(404, response.getStatusLine().getStatusCode());
            assertEquals(//
                    DbHistoricalFinder.NO_DATA_FOUND_FOR_IMSI_OR_MSISDN, //
                    o.get("message").asText()//
            );
            assertEquals(404, o.get("status").asInt());
        }
        {
            HttpGet httpget = new HttpGet(getUrl("" //
                    + "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + HISTDATA
                    + "?imsi=354"//
            ));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            ArrayNode o = (ArrayNode) new ObjectMapper()//
                    .readTree(entity.getContent());
            ObjectWriter writer = new ObjectMapper()//
                    .writerWithDefaultPrettyPrinter();
            System.out.println(writer.writeValueAsString(o));
            assertNotNull(o);
            assertEquals(1, o.size());
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
        {
            HttpGet httpget = new HttpGet(getUrl("" //
                    + "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + HISTDATA
                    + "?imsi=354&imsi=353"//
            ));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            ArrayNode o = (ArrayNode) new ObjectMapper().readTree(entity.getContent());
            ObjectWriter writer = new ObjectMapper()//
                    .writerWithDefaultPrettyPrinter();
            System.out.println(writer.writeValueAsString(o));
            assertNotNull(o);
            assertEquals(2, o.size());
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
        {
            HttpGet httpget = new HttpGet(getUrl("" //
                    + "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + HISTDATA
                    + "?imsi=354&imsi=353&blahblah=blah"//
            ));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            ArrayNode o = (ArrayNode) new ObjectMapper().readTree(entity.getContent());
            ObjectWriter writer = new ObjectMapper()//
                    .writerWithDefaultPrettyPrinter();
            System.out.println(writer.writeValueAsString(o));
            assertNotNull(o);
            assertEquals(2, o.size());
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
        {
            HttpGet httpget = new HttpGet(getUrl("" //
                    + "http://localhost:%s/" + REST_V1_PATH + SUBSCRIBER + HISTDATA
                    + "?imsi=354&imsi=353&blahblah"//
            ));
            HttpResponse response = hc.execute(httpget);
            HttpEntity entity = response.getEntity();
            ArrayNode o = (ArrayNode) new ObjectMapper().readTree(entity.getContent());
            ObjectWriter writer = new ObjectMapper()//
                    .writerWithDefaultPrettyPrinter();
            System.out.println(writer.writeValueAsString(o));
            assertNotNull(o);
            assertEquals(2, o.size());
            assertEquals(200, response.getStatusLine().getStatusCode());
        }

    }

}
