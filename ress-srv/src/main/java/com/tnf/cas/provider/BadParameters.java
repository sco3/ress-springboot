package com.tnf.cas.provider;

import javax.ws.rs.BadRequestException;

public class BadParameters extends BadRequestException {
    private static final long serialVersionUID = 1L;

    public BadParameters(String msg) {
        super(msg);
    }
}
