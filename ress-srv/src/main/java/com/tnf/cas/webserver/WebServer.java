package com.tnf.cas.webserver;

import java.net.ServerSocket;
import java.net.URL;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.fasterxml.jackson.databind.SerializationFeature;

import com.tnf.cas.db.Switcher;
import com.tnf.cas.provider.Informer;
import com.tnf.cas.provider.StatusFilter;
import com.tnf.cas.web.WebServerConstants;
import com.tnf.cascfg.ContextCfg;

public class WebServer implements WebServerConstants {

	private static final Logger mTrace = LoggerFactory.getLogger(WebServer.class);

	private Server mServer;
	private ServletHolder mServlet;

	private SslSocketConnector mSecureConnector;
	private SocketConnector mConnector;

	private String mBeans = ContextCfg.class.getName();
	private boolean mRandomPorts;

	private WebInitiator mInit;

	private WebApplicationContext mAppCtx;

	public void init() throws Exception {

		mServer = new Server();
		mServer.setStopAtShutdown(true);
		mServer.setSendServerVersion(false);

		HandlerCollection collection = new HandlerCollection();
		collection.addHandler(createRestContext("/"));
		mServer.setHandler(collection);

		mServer.start();

		mAppCtx = ContextLoader.getCurrentWebApplicationContext();
		mTrace.info("{}", mAppCtx);
		if (null == mAppCtx) {
			mTrace.error("--- Application context is invalid. Stop. ---");
			mServer.stop();
		} else {
			mTrace.info("*** Application context loaded. Run. ***");
		}
		Informer informer = mAppCtx.getBean(Informer.class);
		informer.setServer(mServer);

		mInit = mAppCtx.getBean(WebInitiator.class);

		mTrace.info(//
				mInit.getValues()//
		);

		Switcher switcher = mAppCtx.getBean(Switcher.class);
		mTrace.info(ReflectionToStringBuilder.toString(//
				switcher, ToStringStyle.MULTI_LINE_STYLE//
		));

		if (mRandomPorts) {
			mInit.setPort(0);
			mInit.setSecurePort(0);
		}

		if (mInit.getThreadPoolSize() > 0) {
			QueuedThreadPool pool = new QueuedThreadPool(mInit.getThreadPoolSize()//
			);
			pool.start();
			mServer.setThreadPool(pool);
		}

		addConnector();
		if (mInit.getSecurePort() >= 0) {
			addSslConnector();
		}

		String resources = mInit.getResources();
		if (resources != null && resources.isEmpty() == false) {
			mServer.addHandler(createStaticContext("/cfg"));
		}

		for (Connector conn : mServer.getConnectors()) {
			conn.setStatsOn(true);
		}

	}

	private Context createRestContext(String path) {

		Context ctx = new Context();
		ctx.setContextPath(path);
		Map<String, String> params = new HashMap<String, String>();

		params.put(ContextLoader.CONTEXT_CLASS_PARAM, AnnotationConfigWebApplicationContext.class.getName());
		// params.put(ContextLoader.CONFIG_LOCATION_PARAM, getBeans());
		params.put(ContextLoader.CONFIG_LOCATION_PARAM, getBeans());
		ctx.setInitParams(params);

		EventListener[] eventListeners = new EventListener[] { new ContextLoaderListener() };
		ctx.setEventListeners(eventListeners);
		mServlet = ctx.addServlet(ServletContainer.class, "/*");
		mServlet.setInitParameter(//
				ServerProperties.PROVIDER_PACKAGES, //
				StatusFilter.class.getPackage().getName() //
		);

		mServlet.setInitParameter(SerializationFeature.FAIL_ON_EMPTY_BEANS.toString(), "false");
		mServlet.setInitOrder(1);
		return ctx;
	}

	private WebAppContext createStaticContext(String path) {
		WebAppContext ctx = new WebAppContext();
		ctx.setContextPath(path);
		if (mInit.getResources() != null) {
			ctx.setResourceBase(mInit.getResources());
		} else {
			ctx.setResourceBase("/dev/null");
		}
		mTrace.info("Context: {} base:{}", path, ctx.getResourceBase());

		ctx.setWelcomeFiles(new String[] { "index.html" });
		Map<String, String> params = new HashMap<String, String>();
		params.put("org.mortbay.jetty.servlet.Default.dirAllowed", "false");

		ctx.setInitParams(params);
		return ctx;
	}

	public void shutdown() {
		try {
			if (mServer != null) {
				mServer.stop();
			}
		} catch (Exception ex) {
			mTrace.error("", ex);
		}
	}

	public Server getWebServer() {
		return mServer;
	}

	public ServletHolder getServlet() {
		return mServlet;
	}

	// public void setResources(String string) {
	// mResources = string;
	// }

	/**
	 * Generate server side jks keystore first
	 * 
	 * <pre>
	 * keytool -genkey -alias server -keyalg RSA -keystore server-keystore.jks
	 * </pre>
	 */
	public void addSslConnector() {
		try {
			String jksFile = mInit.getJksFile();

			mTrace.info("SSL Key Store: {}", jksFile);

			URL resource = Thread.currentThread()//
					.getContextClassLoader()//
					.getResource(jksFile);

			if (resource == null) {
				mTrace.error("Cannot find Key Store: {}", jksFile);
				return;
			}
			String path = resource.getPath();

			mSecureConnector = new SslSocketConnector();
			mSecureConnector.setPort(mInit.getSecurePort());
			mSecureConnector.setHost(mInit.getHost());
			mSecureConnector.setPassword(mInit.getKeystorePass());
			mSecureConnector.setKeyPassword(mInit.getKeyPass());
			mSecureConnector.setKeystore(path);

			mServer.addConnector(mSecureConnector);
			mSecureConnector.start();
			SSLContext dc = SSLContext.getDefault();
			String[] protocols = dc.getDefaultSSLParameters().getProtocols();
			String provider = dc.getProvider().getName();
			String protInfo = String.join(", ", Arrays.asList(protocols));
			mTrace.info("HTTPS/SSL Protocols: {} by {}", protInfo, provider);
			mTrace.info("SSL Key Store Path: {}", path);
		} catch (Exception e) {
			mTrace.error("Cannot add SSL connector", e);
		}
	}

	public void addConnector() {
		try {
			mConnector = new SocketConnector();

			mConnector.setPort(mInit.getPort());
			mConnector.setHost(mInit.getHost());

			mServer.addConnector(mConnector);
			mConnector.start();
		} catch (Exception e) {
			mTrace.error("Cannot set connector up: {}", e.getMessage());
		}
	}

	public void setRandomPorts(boolean b) {
		mRandomPorts = b;

	}

	public int getOpenedPort() {
		int port = mInit.getPort();
		Object o = mConnector.getConnection();
		if (o instanceof ServerSocket) {
			port = ((ServerSocket) o).getLocalPort();
		}
		return port;
	}

	public void setPort(int port) {
		mInit.setPort(port);
	}

	public int getOpenedSecurePort() {
		int port = mInit.getSecurePort();
		Object o = mSecureConnector.getConnection();
		if (o instanceof ServerSocket) {
			port = ((ServerSocket) o).getLocalPort();
		}
		return port;
	}

	public void setSecurePort(int securePort) {
		mInit.setSecurePort(securePort);
	}

	public String getBeans() {
		return mBeans;
	}

	public void setBeans(String beans) {
		mBeans = beans;
	}

	public WebApplicationContext getAppCtx() {
		return mAppCtx;
	}
}
