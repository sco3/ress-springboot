package com.tnf.cas.security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.regex.Pattern;

import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.datastax.driver.core.ConsistencyLevel;
import com.tnf.cas.db.DummyHistFinder;
import com.tnf.cas.db.HistoricalFinder;
import com.tnf.cas.web.WebServerConstants;

import test.spring.non.scannable.TestContextCfg;

public class TestPasswordsAndKeys implements WebServerConstants {

	private AnnotationConfigWebApplicationContext mApp;

	@Before
	public void setup() {

		mApp = new AnnotationConfigWebApplicationContext();
		mApp.setConfigLocations(TestContextCfg.class.getName());
		// "file:src/main/assembly/cfg/beans.xml", "classpath:test-beans.xml"
		mApp.refresh();
	}

	@After
	public void cleanup() {
		mApp.close();
	}

	@Test
	public void testPasswordFile() {
		PropsFileUserStorage storage = new PropsFileUserStorage();

		assertTrue(storage.isValid("tnf", "tnf"));
		assertTrue(storage.isValid("boss", "boss"));
		assertTrue(storage.isValid("test", "test"));

		assertFalse(storage.isValid("tnf", "tnf1"));
		assertFalse(storage.isValid("boss", "boss1"));
		assertFalse(storage.isValid("test", "test1"));

	}

	@Test
	public void testSecret() {
		PasswordFileLoginProvider login = mApp.getBean(PasswordFileLoginProvider.class);
		KeyProvider provider = login.getKeyProvider();
		byte[] key1 = provider.getCurrentKey().getSecret();
		assertNotNull(key1);
		byte[] key2 = provider.getCurrentKey().getSecret();
		assertNotNull(key2);
		assertArrayEquals(key1, key2);
	}

	@Test
	public void testToken() {
		System.out.println(Long.toHexString(-1413241811665637525L));
	}

	@Test
	public void testGenSecret() {
		byte[] secret = new HmacAuthenticator().generateSecret();
		assertNotNull(secret);
		System.out.println(new String(Base64.encodeBase64Chunked(secret)));

	}

	@Test
	public void testValidation() throws Exception {

		PasswordFileLoginProvider login = mApp.getBean(PasswordFileLoginProvider.class);

		{
			long start = System.currentTimeMillis();
			System.out.println(new Date());
			String token = login.login("tnf", "tnf");
			System.out.println(token);
			assertNotNull(token);
			SecurityContext ctx = login.validate(token);
			assertNotNull("Validation failed", ctx);
			assertEquals("tnf", ctx.getUserPrincipal().getName());
			assertTrue(ctx.isUserInRole(HRCC_ROLE));

			String[] parts = token.split(Pattern.quote(":"));
			assertNotNull(parts);
			assertEquals(4, parts.length);
			String eHours = parts[2];
			long finish = Long.parseLong(eHours, Character.MAX_RADIX) * 60 * 1000;
			Date d = new Date(finish);
			System.out.println(d);

			int minsToLive = login.getMinutesToLive();
			assertEquals(480, minsToLive);

			assertTrue((finish - start) / MINUTE - minsToLive >= 0);
			assertTrue((finish - start) / MINUTE - minsToLive <= 1);
		}
		{
			String token = "";
			SecurityContext ctx = login.validate(token);
			assertNull(ctx);
		}
	}

	@Test
	public void testLevel() {
		HistoricalFinder db = mApp.getBean(HistoricalFinder.class);
		System.out.println(db);
		if (db instanceof DummyHistFinder) {
			DummyHistFinder d = (DummyHistFinder) db;
			assertEquals(ConsistencyLevel.TWO, d.getConsistencyLevel());
		}

	}

}
