package com.tnf.cas.provider;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.tnf.cas.web.WebServerConstants;

@Component
@Path(WebServerConstants.REST_V1_PATH)
public class RefresherRest implements ApplicationContextAware {

    private AnnotationConfigWebApplicationContext mCtx;
    Logger mTrace = LoggerFactory.getLogger(RefresherRest.class);

    @GET
    @Path("/refresh")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public String stop() {
        if (mCtx != null) {
            mTrace.info("Refresh.");
            mCtx.refresh();
        } else {
            mTrace.info("Not refreshable.");
        }
        return "ok";
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        if (ctx instanceof AnnotationConfigWebApplicationContext) {
            mCtx = (AnnotationConfigWebApplicationContext) ctx;
        }

    }
}
