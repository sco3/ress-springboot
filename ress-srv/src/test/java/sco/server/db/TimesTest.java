package sco.server.db;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static sco.server.db.TimeSniper.getDates;

import java.util.SortedSet;

import org.junit.Test;

import sco.server.provider.BadParameters;

public class TimesTest {

    @Test
    public void test() {
        {
            String t1 = "20161117010000";
            String t2 = "20161117010000";
            SortedSet<String> dates = getDates(t1, t2, "H");
            out.println(dates);
            assertEquals("[20161117010000]", dates.toString());
        }

        {
            String t1 = "20161117010001";
            String t2 = "20161117050002";
            SortedSet<String> dates = getDates(t1, t2, "H");
            out.println(dates);
            assertEquals(
                    "[20161117010000, 20161117020000, 20161117030000, 20161117040000, 20161117050000]",
                    dates.toString());
        }
        {
            String t1 = "20161117010000";
            String t2 = "20161117050000";
            SortedSet<String> dates = getDates(t1, t2, "H");
            out.println(dates);
            assertEquals(
                    "[20161117010000, 20161117020000, 20161117030000, 20161117040000, 20161117050000]",
                    dates.toString());

        }

        {
            String t1 = "20161117010000";
            String t2 = "20161118010000";
            SortedSet<String> dates = getDates(t1, t2, "D");
            out.println(dates);
            assertEquals("[20161117000000, 20161118000000]", dates.toString());

        }

    }

    @Test
    public void testStephanoQuestions() {
        {
            String t1 = "20170701010203";
            String t2 = "20170702000001";
            SortedSet<String> dates = getDates(t1, t2, "D");
            System.out.println("Time from: " + t1 + " time to: " + t2 + dates);
            assertEquals("[20170701000000, 20170702000000]", dates.toString());
        }
        {
            String t1 = "201706271305";
            String t2 = "201706281305";
            SortedSet<String> dates = getDates(t1, t2, "H");
            System.out.println("Time from: " + t1 + " time to: " + t2 + "->" + dates);
            assertEquals(
                    "[20170627130000, 20170627140000, 20170627150000, 20170627160000, 20170627170000, 20170627180000, 20170627190000, 20170627200000, 20170627210000, 20170627220000, 20170627230000, 20170628000000, 20170628010000, 20170628020000, 20170628030000, 20170628040000, 20170628050000, 20170628060000, 20170628070000, 20170628080000, 20170628090000, 20170628100000, 20170628110000, 20170628120000, 20170628130000]",
                    dates.toString());
        }

    }

    @Test
    public void testSingleTimeid1() {
        String t1 = null;
        String t2 = "20170701010203";
        SortedSet<String> dates = getDates(t1, t2, "D");
        System.out.println("Time from: " + t1 + " time to: " + t2 + dates);
        assertEquals("[20170701000000]", dates.toString());
    }

    @Test
    public void testSingleTimeid2() {
        String t1 = "20170701010203";
        String t2 = null;
        SortedSet<String> dates = getDates(t1, t2, "D");
        System.out.println("Time from: " + t1 + " time to: " + t2 + dates);
        assertEquals("[20170701000000]", dates.toString());
    }

    @Test
    public void testNulls() {
        String t1 = null;
        String t2 = null;
        String msg = null;
        int status = 0;
        try {
            SortedSet<String> dates = getDates(t1, t2, "D");
            System.out.println(dates);
        } catch (BadParameters e) {
            msg = e.getMessage();
            status = e.getResponse().getStatus();
        }
        assertEquals(400, status);
        assertEquals("Wrong Time Period", msg);
    }

}
