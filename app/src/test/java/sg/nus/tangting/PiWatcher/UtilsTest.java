package sg.nus.tangting.PiWatcher;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testTimestampToDate() throws Exception {
        String date = Utils.timestampToDate(1466056186, null);

        System.out.print(date);
        assertEquals("2016/6/16 13:49:46", date);
    }

    @Test
    public void testMd5() throws Exception {
        String origin = "4e860e0a-316d-11e6-bc0d-b827ebbde7cc123456";
        String mdd5expect = "aefb24cd98ebdb72e02be148796e3ff3";
        String result = Utils.md5(origin);

        assertEquals(mdd5expect,result);
    }
}