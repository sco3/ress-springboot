package sco.server.provider;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ExceptionHandler
        implements ExceptionMapper<java.lang.RuntimeException> {
    Logger mTrace = LoggerFactory.getLogger(ExceptionHandler.class);

    public static class Status {
        String mMessage;
        int mStatusCode;

        public Status(int code, String msg) {
            mStatusCode = code;
            mMessage = msg;
        }

        public int getStatus() {
            return mStatusCode;
        }

        public String getMessage() {
            return mMessage;
        }
    }

    @Override
    public Response toResponse(RuntimeException exception) {

        if (exception instanceof BadParameters) {
            BadParameters bp = (BadParameters) exception;
            mTrace.debug(bp.getMessage());
            int code = bp.getResponse().getStatus();
            Status status = new Status(code, bp.getMessage());
            return Response//
                    .status(code)//
                    .entity(status)//
                    .type(MediaType.APPLICATION_JSON)//
                    .build();
        }

        if (exception instanceof ClientErrorException) {
            ClientErrorException cee = (ClientErrorException) exception;
            return cee.getResponse();
        }

        mTrace.warn("{}", exception.getMessage());

        int code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        Status status = new Status(code, "Internal Error");
        return Response//
                .status(Response.Status.INTERNAL_SERVER_ERROR)//
                .entity(status)//
                .type(MediaType.APPLICATION_JSON)//
                .build();
    }
}