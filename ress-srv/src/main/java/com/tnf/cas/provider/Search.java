package com.tnf.cas.provider;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.tnf.cas.db.HistoricalFinder;
import com.tnf.cas.db.Switcher;

import sco.web.WebServerConstants;

@Component("gethistdata")
@Path(WebServerConstants.REST_V1_PATH)
public class Search implements WebServerConstants {

    Logger mAuditor = LoggerFactory.getLogger(Informer.AUDIT);

    private Informer mInformer;

    private HistoricalFinder mFinder;

    @Context
    HttpServletRequest mRequest;

    @Autowired
    Switcher mSwitcher;

    @GET
    @Path(SUBSCRIBER + HISTDATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(value = { HRCC_ROLE })
    public String getHistData( //
            @Context SecurityContext security, //
            @QueryParam(value = "msisdn") List<String> msisdns, //
            @QueryParam(value = "imsi") List<String> imsis, //
            @QueryParam(value = "aggr") String aggr, //
            @QueryParam(value = "timefrom") String timefrom, //
            @QueryParam(value = "timeto") String timeto //

    ) {
        String node = null;
        long start = System.currentTimeMillis();

        int periods = 0;
        int metrics = 0;
        if (getFinder() != null) {
            node = getFinder().find(//
                    Sets.newHashSet(imsis), //
                    Sets.newHashSet(msisdns), //
                    aggr, timefrom, timeto//
            );
            if (node == null || "".equals(node)) {
                node = "[]";
            }
            periods = StringUtils.countMatches(node, "\"time\":");
            metrics = StringUtils.countMatches(node, "\"metricId\":");
            if (periods != 0) {
                metrics /= periods;
            }
        }
        long dur = logMetrics(imsis, msisdns, aggr, timefrom, timeto, start);
        if (getInformer() != null) {
            String user = null;
            if (mSwitcher.isUserStat()) {
                user = security.getUserPrincipal().getName();
            }
            String table = null;
            if (mSwitcher.isTableStat()) {
                if ("h".equalsIgnoreCase(aggr)) {
                    table = mSwitcher.getHourlyTable();

                } else if ("d".equalsIgnoreCase(aggr)) {
                    table = mSwitcher.getDailyTable();

                } else if ("5min".equalsIgnoreCase(aggr)) {
                    table = mSwitcher.getMin5Table();
                }
            }

            if (mSwitcher.isTableStat() || mSwitcher.isUserStat()) {
                getInformer().update(//
                        start, dur, periods, mRequest.isSecure(), //
                        metrics, table, user //
                );

            } else {
                getInformer().update(//
                        start, dur, periods, mRequest.isSecure(), metrics//
                );
            }
        }
        return node;
    }

    private long logMetrics(List<String> imsis, List<String> msisdns,
            String aggr, String timefrom, String timeto, long start) {
        long duration = System.currentTimeMillis() - start;
        if (mAuditor.isDebugEnabled()) {
            mAuditor.debug(//
                    "find:{}:{}:{}:{}:{}:{}:{}", //
                    new Object[] { //
                            start, duration, //
                            String.join(",", imsis), //
                            String.join(",", msisdns), //
                            aggr, timefrom, timeto //
                    });
        }
        return duration;
    }

    public HistoricalFinder getFinder() {
        return mFinder;
    }

    @Autowired
    public void setFinder(HistoricalFinder finder) {
        mFinder = finder;
    }

    public Informer getInformer() {
        return mInformer;
    }

    @Autowired
    public void setInformer(Informer informer) {
        mInformer = informer;
    }
}
