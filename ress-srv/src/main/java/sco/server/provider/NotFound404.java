package sco.server.provider;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sco.server.provider.ExceptionHandler.Status;

@Provider
public class NotFound404 implements ExceptionMapper<NotFoundException> {
    Logger mTrace = LoggerFactory.getLogger(NotFound404.class);

    public Response toResponse(NotFoundException exception) {
        int code = Response.Status.NOT_FOUND.getStatusCode();
        Status status = null;
        if (exception instanceof NoDataFound) {
            status = new Status(code, exception.getMessage());
            return Response//
                    .status(Response.Status.NOT_FOUND)//
                    .entity(status)//
                    .type(MediaType.APPLICATION_JSON)//
                    .build();
        }

        mTrace.warn("{}", exception.getMessage());

        status = new Status(code, "Not found");

        return Response//
                .status(Response.Status.NOT_FOUND)//
                .entity(status)//
                .type(MediaType.APPLICATION_JSON)//
                .build();
    }
}