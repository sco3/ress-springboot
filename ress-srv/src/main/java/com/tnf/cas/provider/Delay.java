package com.tnf.cas.provider;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tnf.cas.web.WebServerConstants;

@Component("delay")
@Path(WebServerConstants.REST_V1_PATH)
public class Delay {
    Logger mTrace = LoggerFactory.getLogger(Delay.class);

    @GET
    @Path("/delay")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public String delay(//
            @QueryParam(value = "delay") int delay, //
            @QueryParam(value = "size") int size //
    ) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < size; i++) {
            b.append("0");
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {

        }
        return b.toString();
    }
}
