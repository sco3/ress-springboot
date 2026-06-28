package com.tnf.cas.security;

import javax.ws.rs.core.SecurityContext;

public interface LoginProvider {
    public String login(String name, String password);

    SecurityContext validate(String token);
}
