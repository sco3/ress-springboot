package com.tnf.cas.provider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Session;

@Component
public class Informer implements FactoryBean<Informer> {
    public static int HIST_TOP_MS = 4000;
    public static int HIST_STEP_MS = 500;
    public static int HIST_STEPS = HIST_TOP_MS / HIST_STEP_MS + 1;
    public static int[] mHistMargins = new int[HIST_STEPS];
    int[] hCnt = new int[HIST_STEPS];
    public static final String AUDIT = "audit";
    public static final String CONSTAT = "constat";
    long PERIOD = 60000L;
    Logger mAuditor = LoggerFactory.getLogger(AUDIT);
    Logger mConstat = LoggerFactory.getLogger(CONSTAT);
    Logger mTrace = LoggerFactory.getLogger(Informer.class);
    Queue<InformerRecord> mQueue = new ConcurrentLinkedQueue<InformerRecord>();

    static Informer mInst = null;
    private SimpleDateFormat mDf;
    private Server mServer;
    private Session mSession;
    private Set<String> mTables = new HashSet<String>();
    private HashMap<String, Integer> mUsers = new HashMap<String, Integer>();

    public Informer() {
        mDf = new SimpleDateFormat("yyyyMMddHHmm");
        mDf.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (int i = 0; i < mHistMargins.length; i++) {
            mHistMargins[i] = HIST_STEP_MS * (i + 1);
        }
        mHistMargins[mHistMargins.length - 1] = Integer.MAX_VALUE;
    }

    public static synchronized Informer getInst() {
        if (mInst == null) {
            mInst = new Informer();
            mInst.start();
        }
        return mInst;
    }

    public String calc() {
        long end = System.currentTimeMillis();
        long cnt = 0;
        long dur = 0;
        long max = 0;
        long secure = 0;
        double timeIds = 0;
        long metrics = 0;
        String hist = "";
        for (int i = 0; i < hCnt.length; i++) {
            hCnt[i] = 0;
        }
        mTables.clear();
        mUsers.clear();
        while (mQueue.peek() != null && mQueue.peek().mStart < end) {
            InformerRecord record = mQueue.poll();
            cnt++;
            dur += record.mDuration;
            if (record.mDuration > max) {
                max = record.mDuration;
            }
            timeIds += record.mTimeIds;
            if (record.mSecure) {
                secure++;
            }
            metrics += record.mMetrics;
            for (int i = 0; i < mHistMargins.length; i++) {
                if (record.mDuration <= mHistMargins[i]) {
                    hCnt[i]++;
                    break;
                }
            }
            if (record.mTable != null) {
                mTables.add(record.mTable);
            }
            String user = record.mUser;
            if (user != null) {
                Integer uHit = mUsers.get(user);
                if (uHit == null) {
                    mUsers.put(user, 1);
                } else {
                    mUsers.put(user, uHit.intValue() + 1);
                }
            }
        }
        for (int i = 0; i < HIST_STEPS; i++) {
            // hCnt[i] = ;
            hist += "~" + Math.round(hCnt[i] * 100.0 / cnt);
        }
        long avg = cnt == 0 ? 0 : dur / cnt;
        long avgTimeIds = Math.round((cnt == 0 ? 0 : timeIds / cnt));
        long avgSecure = Math.round((cnt == 0 ? 0 : 100 * secure / cnt));
        long avgMetrics = Math.round((cnt == 0 ? 0 : metrics / cnt));
        String fmt = ("" //
                + "stat: {}" //
                + " count: {}/minute count: {}/sec avg: {} ms" //
                + " max: {} ms avgTimeIds: {} avgSecure: {}%" //
                + " avgMetrics: {} histogram: {}~ table: {}" //
                + " users: {}" //
        );
        String dt = mDf.format(new Date(end));
        mAuditor.info(fmt, //
                new Object[] { //
                        dt, //
                        cnt, //
                        cnt / 60, //
                        avg, //
                        max, //
                        avgTimeIds, //
                        avgSecure, //
                        avgMetrics, //
                        hist, //
                        mTables.toString(), //
                        mUsers.toString() //
                });

        if (mServer != null) {
            for (Connector conn : mServer.getConnectors()) {
                mConstat.info("" //
                        + "constat: {} port: {} connections: {} " //
                        + "dur min: {} dur avg: {} dur max: {} dur total: {} " //
                        + "open: {} open min: {} open max: {} "
                        + "requests: {} req min: {} req avg: {} req max: {} :{}", //
                        new Object[] { //
                                dt, //
                                conn.getPort(), //
                                conn.getConnections(), //
                                conn.getConnectionsDurationMin(), //
                                conn.getConnectionsDurationAve(), //
                                conn.getConnectionsDurationMax(), //
                                conn.getConnectionsDurationTotal(), //
                                conn.getConnectionsOpen(), //
                                conn.getConnectionsOpenMin(), //
                                conn.getConnectionsOpenMax(), //
                                conn.getRequests(), //
                                conn.getConnectionsRequestsMin(), //
                                conn.getConnectionsRequestsAve(), //
                                conn.getConnectionsRequestsMax(), //
                                ")" });
                conn.statsReset();
            }
        }
        return hist;

    }

    public void start() {

        Timer timer = new Timer("info", true);

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (mServer != null) {
                    mTrace.info(//
                            "Server pool threads: {} idle: {} ", //
                            new Object[] { //
                                    mServer.getThreadPool().getThreads(), //
                                    mServer.getThreadPool().getIdleThreads() //
                    }//
                    );
                }
                if (mSession != null) {
                    Session.State state = mSession.getState();
                    ToStringBuilder
                            .setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
                    String msg = "\n"
                            + ToStringBuilder.reflectionToString(state) + "\n";

                    mTrace.info("Cassandra session: {}", msg);
                }
                calc();
            }
        }, PERIOD, PERIOD);
    }

    public void update(//
            long start, long duration, int periods, //
            boolean secure, int metrics//
    ) {
        mQueue.add(//
                new InformerRecord(//
                        start, duration, periods, //
                        secure, metrics, null, null //
                )//
        );
    }

    public void update(//
            long start, long duration, int periods, //
            boolean secure, int metrics, String table, //
            String user //
    ) {
        mQueue.add(//
                new InformerRecord(//
                        start, duration, periods, //
                        secure, metrics, table, user //
                )//
        );
    }

    @Override
    public Informer getObject() throws Exception {
        return getInst();
    }

    @Override
    public Class<?> getObjectType() {
        return Informer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setServer(Server server) {
        mServer = server;
    }

    public Session getSession() {
        return mSession;
    }

    @Autowired
    public void setSession(Session session) {
        mSession = session;
    }

}
