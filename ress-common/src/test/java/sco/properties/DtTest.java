package sco.properties;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.tnf.cas.common.properties.Dt;

public class DtTest {
    @Test
    public void testShiftTime() throws Exception {
        String timeId = "20161117010000";
        long shift = 0;
        String timeUnit = "H";
        String actual = Dt.shiftTime(timeId, shift, timeUnit);
        String expected = "201611170100";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDt() throws Exception {
        String timeId = "";
        String tableName = null;
        Dt.getDt(timeId, tableName);
    }
}
