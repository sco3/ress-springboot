package sco.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class CassandraHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraHelper.class);
    public static final String FLYWAY_TABLE = "flyway_info";
    public static final String CHECK_FLYWAY_TABLE = "select id from system_schema.tables where keyspace_name='$keyspaceName' and table_name='$flywayTable'";
    private static final String CREATE_FLYWAY_TABLE = "create table $flywayTable (table_name text primary key, version text)";

    private CassandraHelper() {
    }

    public static ResultSet execute(Statement statement, Session session) throws Exception {
        statement.setConsistencyLevel(ConsistencyLevel.QUORUM);
        try{
        	return session.execute(statement);
        } catch (Exception e) {
            LOGGER.warn("Can Not execute with ConsistencyLevel.QUORUM, try ConsistencyLevel.ONE" );
        	statement.setConsistencyLevel(ConsistencyLevel.ONE);
        	return session.execute(statement);
        }        
    }

    public static void checkFlywayTable(String keyspaceName, Session session) throws Exception {
        String cql = CHECK_FLYWAY_TABLE //
                .replaceAll("\\$flywayTable", FLYWAY_TABLE) //
                .replaceAll("\\$keyspaceName", keyspaceName);
        Statement statement = new SimpleStatement(cql);
        ResultSet rs = execute(statement, session);
        if (rs.one() == null) {
            cql = CREATE_FLYWAY_TABLE //
                    .replaceAll("\\$flywayTable", FLYWAY_TABLE);
            statement = new SimpleStatement(cql);
            CassandraHelper.execute(statement, session);
        }
    }
}
