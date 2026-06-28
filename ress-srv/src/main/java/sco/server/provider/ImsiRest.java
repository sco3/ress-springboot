package sco.server.provider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sco.server.db.ImsiResolver;
import sco.server.web.WebServerConstants;

@Component("getimsibymsisdn")
@Path(WebServerConstants.REST_V1_PATH)
public class ImsiRest implements WebServerConstants {
    private static final Set<String> EMPTY = new HashSet<String>();

    private ImsiResolver mResolver;

    @GET
    @Path(SUBSCRIBER + IMSI)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = { HRCC_ROLE })
    public Set<String> imsi( //
            @QueryParam(value = "msisdn") List<String> msisdns //
    ) {
        Set<String> result = EMPTY;
        if (getResolver() != null) {
            result = mResolver.find(new HashSet<String>(msisdns)).getImsis();
        }
        return result;
    }

    public ImsiResolver getResolver() {
        return mResolver;
    }

    @Autowired
    public void setResolver(ImsiResolver resolver) {
        mResolver = resolver;
    }
}
