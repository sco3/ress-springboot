package com.tnf.cas.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tnf.cas.web.WebServerConstants;

import test.spring.non.scannable.EclipseContextCfg;

public class EclipseWebServer implements WebServerConstants {
	private static final Logger mTrace = LoggerFactory.getLogger(WebServer.class);

	public static void main(String[] argv) throws Exception {
		Properties props = new Properties();
		File cfgProps = new File("src/main/assembly/cfg/cfg-cas.properties.template");
		if (cfgProps.exists()) {
			try (InputStream is = new FileInputStream(cfgProps)) {
				props.load(is);
			}

			int port = Integer.parseInt(props.getProperty(//
					WEBSERVER_PORT_PROPERTY, "" + DEFAULT_WEBSERVER_PORT//
			));

			try (Socket s = new Socket("0.0.0.0", port)) {
				mTrace.info("Another server is running. Stop it.");
				try (DefaultHttpClient hc = new DefaultHttpClient()) {

					HttpGet httpget = new HttpGet("http://localhost:" + port + "/hrcc/api/v1/stop"//
					);
					hc.execute(httpget);
				} catch (Exception e) {
					mTrace.debug("Server should stop, broken connectin is ok. ");
				}
			} catch (Exception e) {
				mTrace.info("No server is running.");
			}
		}

		WebServer webServer = new WebServer();
		webServer.setBeans(EclipseContextCfg.class.getName());
		webServer.init();
	}
}
