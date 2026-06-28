package com.tnf.cas.provider;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tnf.cas.db.SubProfiler;
import com.tnf.cas.web.WebServerConstants;

@Component("getsubprofile")
@Path(WebServerConstants.REST_V1_PATH)
public class SubProfilerRest implements WebServerConstants {
    private SubProfiler mProfiler;
    private Informer mInformer;

    @GET
    @Path(SUBSCRIBER + PROFILE)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = { HRCC_ROLE })
    public String gerSubProfile( //
            @QueryParam(value = "imsi") Set<String> imsis, //
            @QueryParam(value = "msisdn") Set<String> msisdns //
    ) {
        long start = System.currentTimeMillis();
        String result = "";

        if (getProfiler() != null) {
            result = getProfiler().getProfiles(imsis, msisdns);
        }
        long dur = System.currentTimeMillis() - start;

        if (getInformer() != null) {
            getInformer().update(//
                    start, dur, 0, false, 0//
            );
        }

        return result;
    }

    public SubProfiler getProfiler() {
        return mProfiler;
    }

    @Autowired
    public void setProfiler(SubProfiler p) {
        mProfiler = p;
    }

    public Informer getInformer() {
        return mInformer;
    }

    @Autowired
    public void setInformer(Informer informer) {
        mInformer = informer;
    }

}
