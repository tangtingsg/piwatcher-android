package sg.nus.tangting.PiWatcher;

import android.test.AndroidTestCase;

public class UtilsATest extends AndroidTestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testIsJson() throws Exception {
        String json = "{'uuid':'21a2e1e0-3174-11e6-a368-b827ebbde7cc','psw':'c3421c4140510deb17ea4736bc98d31f'}";
        boolean isJson = Utils.isJson(json);

        assertTrue(isJson);
    }
}
