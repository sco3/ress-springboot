package sco.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import sco.cfg.ContextCfg;
import sco.web.WebServerConstants;

public class WebServer implements WebServerConstants {

	private static final Logger mTrace = LoggerFactory.getLogger(WebServer.class);

	private ConfigurableApplicationContext mAppCtx;
	private String mBeans = ContextCfg.class.getName();
	private boolean mRandomPorts;
	private WebInitiator mInit;

	public void init() throws Exception {
		SpringApplication app = new SpringApplication(Class.forName(mBeans));
		app.setWebEnvironment(true);
		app.setBannerMode(org.springframework.boot.Banner.Mode.OFF);

		if (mRandomPorts) {
			app.setDefaultProperties(java.util.Collections.singletonMap("server.port", "0"));
		}

		mAppCtx = app.run();

		mTrace.info("*** Application context loaded. Run. ***");

		mInit = mAppCtx.getBean(WebInitiator.class);
		mTrace.info(mInit.getValues());
	}

	public void shutdown() {
		if (mAppCtx != null) {
			SpringApplication.exit(mAppCtx);
		}
	}

	public WebApplicationContext getAppCtx() {
		if (mAppCtx instanceof WebApplicationContext) {
			return (WebApplicationContext) mAppCtx;
		}
		return null;
	}

	public void setRandomPorts(boolean b) {
		mRandomPorts = b;
	}

	public int getOpenedPort() {
		if (mAppCtx != null && mAppCtx.getEnvironment() != null) {
			String port = mAppCtx.getEnvironment().getProperty("local.server.port");
			if (port != null) {
				return Integer.parseInt(port);
			}
		}
		return mInit != null ? mInit.getPort() : DEFAULT_WEBSERVER_PORT;
	}

	public void setPort(int port) {
		if (mInit != null) {
			mInit.setPort(port);
		}
	}

	public int getOpenedSecurePort() {
		return mInit != null ? mInit.getSecurePort() : DEFAULT_WEBSERVER_HTTPS_PORT;
	}

	public void setSecurePort(int securePort) {
		if (mInit != null) {
			mInit.setSecurePort(securePort);
		}
	}

	public String getBeans() {
		return mBeans;
	}

	public void setBeans(String beans) {
		mBeans = beans;
	}
}
