package sco.server.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

@Component
public class DbHistoricalSchemaGenerator implements HistoricalSchemaGenerator {
    private Session mSession;

    public Session getSession() {
        return mSession;
    }

    @Autowired
    public void setSession(Session session) {
        mSession = session;
    }

    @Override
    public String getSchema() {
        ResultSet rs = mSession.execute(//
                "select value from cas_properties " //
                        + "where name='metric.definition.all'" //
        );
        for (Row row : rs) {
            return row.getString(0);
        }
        return "";
    }

}
