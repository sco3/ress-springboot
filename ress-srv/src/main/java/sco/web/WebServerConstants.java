package sco.web;

import java.nio.file.Paths;

public interface WebServerConstants {
    static final String CFG_CONTEXT_HTTPD_ROOT_DIR = "cfg.context.httpd.root.dir";

    static final String WEBSERVER_PORT_PROPERTY = "webserver.port";
    static final String WEBSERVER_HOST_PROPERTY = "webserver.host";
    static final String WEBSERVER_HTTPS_PORT_PROPERTY = "webserver.https.port";
    static final String WEBSERVER_KEY_PASSWORD_PROPERTY = "webserver.key.password";
    static final String WEBSERVER_THREAD_POOL_SIZE = "webserver.thread.pool.size";

    static final String WEBSERVER_KEYSTORE_FILE_PROPERTY = "webserver.keystore.file";
    static final String WEBSERVER_KEYSTORE_PASSWORD_PROPERTY = "webserver.keystore.password";
    static final String WEBSERVER_PASSWD = "passwd";

    static final int DEFAULT_WEBSERVER_PORT = 8009;
    static final String DEFAULT_WEBSERVER_HOST = "0.0.0.0";
    static final int DEFAULT_WEBSERVER_HTTPS_PORT = 8010;
    static final String DEFAULT_KEYSTORE_FILE_PROPERTY = "server-keystore.jks";

    static final String HRCC_ROLE = "hrcc";

    static final String REST_TOKEN_NAME = "token";

    static final String REST_V1_PATH = "cci/api/v1";

    static final String SUBSCRIBER = "/subscriber";
    static final String IMSI = "/imsi";
    static final String PROFILE = "/profile";
    static final String HISTDATA = "/histdata";
    static final String DEF = "/def";

    static final String PATH_TO_USER_CONFIG_DIR = Paths.get( //
            "..", "cas-main-var", "cfg").toString();

    static final int MINUTE = 60 * 1000;
    static final String TOKEN_MINUTES_TO_LIVE = "token.minutes.to.live";
    static final String AUTH_METHOD = "auth.method";
    static final String AUTH_SECRET = "auth.secret";

}
