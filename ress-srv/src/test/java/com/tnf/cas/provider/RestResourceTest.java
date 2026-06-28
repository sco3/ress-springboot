package com.tnf.cas.provider;

import java.util.regex.Pattern;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sco.web.WebServerConstants;

@Component
@Path(WebServerConstants.REST_V1_PATH)
public class RestResourceTest implements WebServerConstants {
    Logger mTrace = LoggerFactory.getLogger(RestResourceTest.class);

    private String mStr = "plain";

    public static class Ret {
        public String token;
        public String msg;
    }

    @GET
    @Path("/stop")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public String stop() {
        System.exit(0);
        return "stop";
    }

    @GET
    @Path("/testProtected")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = { HRCC_ROLE })
    public Ret testProtected(@Context ContainerRequestContext requestContext) {
        mTrace.info("Security {}", requestContext.getSecurityContext());
        Ret ret = new Ret();
        ret.msg = "Checked: " + requestContext //
                .getSecurityContext() //
                .getUserPrincipal()//
                .getName();
        return ret;
    }

    @GET
    @Path("/testUnprotected")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Ret testUnprotected(
            @Context ContainerRequestContext requestContext) {
        mTrace.info("Security {}", requestContext.getSecurityContext());
        Ret ret = new Ret();
        ret.token = getStr();
        return ret;
    }

    @GET
    @Path("/testException")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Ret testException(@Context ContainerRequestContext requestContext) {
        throw new RuntimeException("Aha");
    }

    @GET
    @Path("/test3")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Object[] test3(@Context ContainerRequestContext requestContext)
            throws Exception {
        mTrace.info("Security {}", requestContext.getSecurityContext());
        Object[] rets = new Object[4];
        {
            Ret ret = new Ret();
            ret.token = "No Check1";
            rets[0] = ret;
        }
        {
            Ret ret = new Ret();
            ret.token = "No Check2";
            rets[1] = ret;
        }
        {
            String str = "{'a':'b'}".replaceAll(Pattern.quote("'"), "\"");
            rets[2] = new ObjectMapper().readTree(str);
        }
        ObjectNode o = new ObjectMapper().createObjectNode();
        o.put("a2", "b2");
        rets[4] = o;
        return rets;
    }

    public String getStr() {
        return mStr;
    }

    @Autowired(required = false)
    @Qualifier("testString")
    public void setStr(String str) {
        mStr = str;
    }

}
