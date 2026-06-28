package com.tnf.cas.provider;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request,
            ContainerResponseContext response) throws IOException {
        ;
        response.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        // response.getHeaders().add("Access-Control-Allow-Headers",
        // "origin, content-type, accept, authorization");
        response.getHeaders().add(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        // response.getHeaders().add("Access-Control-Allow-Methods",
        // "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}