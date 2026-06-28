package sco.server.web;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebInitiator implements WebServerConstants, Cloneable {
    // private static final Logger mTrace = LoggerFactory
    // .getLogger(WebInitiator.class);

    @Value("${" + WEBSERVER_HOST_PROPERTY + "}")
    private String mHost = DEFAULT_WEBSERVER_HOST;

    @Value("${" + WEBSERVER_PORT_PROPERTY + "}")
    private int mPort = DEFAULT_WEBSERVER_PORT;

    @Value("${" + WEBSERVER_HTTPS_PORT_PROPERTY + "}")
    private int mSecurePort;

    @Value("${" + WEBSERVER_KEYSTORE_FILE_PROPERTY + "}")
    private String mKeyStoreFile;

    @Value("${" + CFG_CONTEXT_HTTPD_ROOT_DIR + ":}")
    private String mResources;

    @Value("${" + WEBSERVER_KEYSTORE_FILE_PROPERTY + "}")
    private String mJksFile;

    @Value("${" + WEBSERVER_KEYSTORE_PASSWORD_PROPERTY + "}")
    private String mKeystorePass;

    @Value("${" + WEBSERVER_KEY_PASSWORD_PROPERTY + "}")
    private String mKeyPass;

    @Value("${" + WEBSERVER_THREAD_POOL_SIZE + "}")
    private int mThreadPoolSize;

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public int getSecurePort() {
        return mSecurePort;
    }

    public void setSecurePort(int securePort) {
        mSecurePort = securePort;
    }

    public String getKeyStoreFile() {
        return mKeyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        mKeyStoreFile = keyStoreFile;
    }

    public String getResources() {
        return mResources;
    }

    public void setResources(String resources) {
        mResources = resources;
    }

    public String getJksFile() {
        return mJksFile;
    }

    public void setJksFile(String jksFile) {
        mJksFile = jksFile;
    }

    public String getKeystorePass() {
        return mKeystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        mKeystorePass = keystorePass;
    }

    public String getKeyPass() {
        return mKeyPass;
    }

    public void setKeyPass(String keyPass) {
        mKeyPass = keyPass;
    }

    public int getThreadPoolSize() {
        return mThreadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        mThreadPoolSize = threadPoolSize;
    }

    public String getHost() {
        String host = System.getProperty(WEBSERVER_HOST_PROPERTY);
        if (host != null) {
            mHost = host;
        }
        return mHost;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public String getValues() {
        String result = "";
        try {
            WebInitiator inits = (WebInitiator) this.clone();
            inits.mKeyPass = scramble(inits.mKeyPass);
            inits.mKeystorePass = scramble(inits.mKeystorePass);
            ReflectionToStringBuilder
                    .setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
            result = ReflectionToStringBuilder.toString(//
                    inits, ToStringStyle.MULTI_LINE_STYLE//
            );
        } catch (Exception e) {

        }
        return result;
    }

    public static String scramble(String any) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < any.length(); i++) {
            b.append((char) (0x55 ^ any.charAt(i)));
        }
        return b.toString();
    }

}
