package com.tnf.cas.provider;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.tnf.cas.security.LoginProvider;

import sco.web.WebServerConstants;

@Component
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class TokenValidator
        implements ContainerRequestFilter, WebServerConstants {

    private Logger mTrace = LoggerFactory.getLogger(TokenValidator.class);

    @Autowired
    @Qualifier("loginProvider")
    private LoginProvider mLoginProvider;

    @Context
    HttpServletRequest mRequest;

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        try {
            Map<String, Cookie> cookies = requestContext.getCookies();
            if (mLoginProvider != null) {
                String tok = null;
                Cookie cookie = cookies.get(REST_TOKEN_NAME);
                if (cookie != null) {
                    tok = cookie.getValue();
                }
                if (tok == null || tok.isEmpty()) {
                    tok = mRequest.getParameter(REST_TOKEN_NAME);
                    mTrace.debug(tok);
                }
                if (tok != null) {
                    SecurityContext ctx = mLoginProvider.validate(tok);
                    if (ctx != null) {
                        requestContext.setSecurityContext(ctx);
                    }
                }
            }
        } catch (Exception e) {
            mTrace.error("{}", e);
        }
    }

    public LoginProvider getLoginProvider() {
        return mLoginProvider;
    }

    public void setLoginProvider(LoginProvider loginProvider) {
        mLoginProvider = loginProvider;
    }
}
