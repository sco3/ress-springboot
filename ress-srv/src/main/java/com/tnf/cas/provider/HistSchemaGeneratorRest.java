package com.tnf.cas.provider;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tnf.cas.db.HistoricalSchemaGenerator;
import com.tnf.cas.web.WebServerConstants;

@Component("gethistdatadef")
@Path(WebServerConstants.REST_V1_PATH)
public class HistSchemaGeneratorRest implements WebServerConstants {

    private HistoricalSchemaGenerator mSchemaGenerator;

    @GET
    @Path(SUBSCRIBER + HISTDATA + DEF)
    @RolesAllowed(value = { HRCC_ROLE })
    public String getHistSchema( //
    ) {

        String node = null;
        if (getSchemaGenerator() != null) {
            node = getSchemaGenerator().getSchema();
        }
        return node;
    }

    public HistoricalSchemaGenerator getSchemaGenerator() {
        return mSchemaGenerator;
    }

    @Autowired
    public void setSchemaGenerator(HistoricalSchemaGenerator schemaGenerator) {
        mSchemaGenerator = schemaGenerator;
    }
}
