package sco.provider;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;

import sco.web.WebServerConstants;

@Provider
public class StatusFilter
        implements ContainerResponseFilter, WebServerConstants {

    class Status {
        private StatusType mStatus;

        public Status(StatusType status) {
            mStatus = status;
        }

        public int getStatus() {
            return mStatus.getStatusCode();
        }

        public String getMessage() {
            return mStatus.getReasonPhrase();
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {

        if (responseContext.getEntity() == null) {
            StatusType statusInfo = responseContext.getStatusInfo();
            responseContext.setEntity(new Status(statusInfo));
        }
    }
}
