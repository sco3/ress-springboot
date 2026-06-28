package sco.server.provider;

import javax.ws.rs.NotFoundException;

public class NoDataFound extends NotFoundException {
    private static final long serialVersionUID = 1L;

    public NoDataFound(String msg) {
        super(msg);
    }
}
