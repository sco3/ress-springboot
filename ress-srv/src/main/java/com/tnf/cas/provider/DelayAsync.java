package com.tnf.cas.provider;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sco.web.WebServerConstants;

@Component("delayasync")
@Path(WebServerConstants.REST_V1_PATH)
public class DelayAsync {
    Logger mTrace = LoggerFactory.getLogger(Delay.class);

    @GET
    @Path("/delayasync")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public void delay(//
            @QueryParam(value = "delay") int delay, //
            @QueryParam(value = "size") int size, //
            @Suspended final AsyncResponse asyncResponse//
    ) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    b.append("0");
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {

                }
                asyncResponse.resume(b.toString());
            }

        }).start();
    }
}
