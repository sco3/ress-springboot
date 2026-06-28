package com.tnf.cas.provider;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.tnf.cas.db.ProfileSchemaGenerator;
import com.tnf.cas.web.WebServerConstants;

@Component("getsubprofiledef")
@Path(WebServerConstants.REST_V1_PATH)
public class SubProfileGeneratorRest implements WebServerConstants {

    private ProfileSchemaGenerator mSchemaGenerator;

    @GET
    @Path(SUBSCRIBER + PROFILE + DEF)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = { HRCC_ROLE })
    public JsonNode getSubProfileDev( //
    ) {
        JsonNode node = null;
        if (getSchemaGenerator() != null) {
            node = getSchemaGenerator().generateSchema();
        }
        return node;
    }

    public ProfileSchemaGenerator getSchemaGenerator() {
        return mSchemaGenerator;
    }

    @Autowired
    public void setSchemaGenerator(ProfileSchemaGenerator schemaGenerator) {
        mSchemaGenerator = schemaGenerator;
    }
}
