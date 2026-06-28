package com.tnf.cas.db;

import static sco.common.db.CassandraDatasourceFactory.CASSANDRA_HOSTS_COMMA_SEPARATED;
import static sco.common.db.CassandraDatasourceFactory.CASSANDRA_KEYSPACE;
import static sco.common.db.CassandraDatasourceFactory.CASSANDRA_PORT;
import static sco.common.db.CassandraDatasourceFactory.CREATE_KEYSPACE_CQL_PROPERTY_NAME;
import static sco.common.db.CassandraDatasourceFactory.PASSWORD_PROPERTY_NAME;
import static sco.common.db.CassandraDatasourceFactory.USER_PROPERTY_NAME;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

import sco.common.db.CassandraDatasourceFactory;
import sco.common.util.CasPropertiesHelper;
import sco.web.WebServerConstants;

@Component
public class DbSession //
        implements WebServerConstants, FactoryBean<Session>, DisposableBean {
    static final Logger mTrace = LoggerFactory.getLogger(DbSession.class);
    static int mDynamicPort;

    private Properties mProps = new Properties();
    private Session mSession = null;

    @Override
    public Session getObject() {
        try {
            if (mProps.size() == 0) {
                mProps = CasPropertiesHelper.getCassandraConnectionProperties();
            }
            mSession = CassandraDatasourceFactory.getSession(mProps);
        } catch (NoHostAvailableException e) {
            mTrace.error("{}", e.getMessage());
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
        return mSession;
    }

    @Override
    public void destroy() throws Exception {
        mTrace.info("Close Cassandra Session");
        if (mSession != null) {
            mSession.getCluster().close();
            mSession = null;
            CassandraDatasourceFactory.getSessionMap().put(//
                    CassandraDatasourceFactory.getSessionMapKey(mProps), //
                    mSession //
            );
        }
    }

    @Override
    public Class<?> getObjectType() {
        return Session.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Value("${" + CASSANDRA_PORT + "}")
    public void setPort(int port) {
        if (mDynamicPort > 0) {
            mProps.setProperty(CASSANDRA_PORT, Integer.toString(mDynamicPort));
        } else {
            mProps.setProperty(CASSANDRA_PORT, Integer.toString(port));
        }
    }

    public String getHostsLine() {
        return mProps.getProperty(CASSANDRA_HOSTS_COMMA_SEPARATED);
    }

    @Value("${" + CASSANDRA_HOSTS_COMMA_SEPARATED + "}")
    public void setHostsLine(String hostsLine) {
        mProps.setProperty(CASSANDRA_HOSTS_COMMA_SEPARATED, hostsLine);
    }

    public String getKeySpace() {
        return mProps.getProperty(CASSANDRA_KEYSPACE);
    }

    @Value("${" + CASSANDRA_KEYSPACE + "}")
    public void setKeySpace(String keySpace) {
        mProps.setProperty(CASSANDRA_KEYSPACE, keySpace);
    }

    @Value("${" + CREATE_KEYSPACE_CQL_PROPERTY_NAME + "}")
    public void setKeySpaceCql(String cql) {
        mProps.setProperty(CREATE_KEYSPACE_CQL_PROPERTY_NAME, cql);
    }

    public String getUserName() {
        return mProps.getProperty(USER_PROPERTY_NAME);
    }

    @Value("${" + USER_PROPERTY_NAME + "}")
    public void setUserName(String userName) {
        mProps.setProperty(USER_PROPERTY_NAME, userName);
    }

    public String getPassword() {
        return mProps.getProperty(PASSWORD_PROPERTY_NAME);
    }

    @Value("${" + PASSWORD_PROPERTY_NAME + "}")
    public void setPassword(String password) {
        mProps.setProperty(PASSWORD_PROPERTY_NAME, password);
    }

    public static void setDynamicPort(int port) {
        mDynamicPort = port;
    }

    public String toString() {
        return ("" //
                + " Hosts: " + mProps.getProperty(CASSANDRA_HOSTS_COMMA_SEPARATED) //
                + " Port: " + mProps.getProperty(CASSANDRA_PORT) //
                + " KeySpace: " + mProps.getProperty(CASSANDRA_KEYSPACE) //
        );
    }

}
