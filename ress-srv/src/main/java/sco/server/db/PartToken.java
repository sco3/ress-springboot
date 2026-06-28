package sco.server.db;

public class PartToken {

    private String mSgm;
    private String mDt;

    public PartToken(String dt, String sgm) {
        mSgm = sgm;
        mDt = dt;
    }

    private String join() {
        return getSgm() + ":" + getDt();
    }

    @Override
    public boolean equals(Object o) {
        return join().equals(((PartToken) o).join());
    }

    @Override
    public String toString() {
        return join();
    }

    @Override
    public int hashCode() {
        return join().hashCode();
    }

    public String getSgm() {
        return mSgm;
    }

    public void setSgm(String sgm) {
        mSgm = sgm;
    }

    public String getDt() {
        return mDt;
    }

    public void setDt(String dt) {
        mDt = dt;
    }
}
