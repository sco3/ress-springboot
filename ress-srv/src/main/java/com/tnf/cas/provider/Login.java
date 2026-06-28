package com.tnf.cas.provider;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tnf.cas.security.LoginProvider;

import sco.web.WebServerConstants;

@Component
@Path(WebServerConstants.REST_V1_PATH)
public class Login {

    private LoginProvider mLoginProvider;

    public static class Ret {
        public String token;
    }

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login( //
            @QueryParam(value = "user") String user, //
            @QueryParam(value = "password") String password //
    ) {
        String token = null;
        if (mLoginProvider != null) {
            token = mLoginProvider.login(user, password);
        }

        Response resp = null;
        if (token != null) {
            Ret ret = new Ret();
            ret.token = token;
            NewCookie cookie = new NewCookie("token", ret.token);
            resp = Response.ok()//
                    .entity(ret) //
                    .cookie(cookie)//
                    .build();

        } else {
            resp = Response.status(Status.FORBIDDEN)//
                    .cookie(new NewCookie("token", ""))//
                    .build();
        }
        return resp;
    }

    public LoginProvider getLoginProvider() {
        return mLoginProvider;
    }

    @Autowired
    @Qualifier("loginProvider")
    public void setLoginProvider(LoginProvider loginProvider) {
        mLoginProvider = loginProvider;
    }

}
