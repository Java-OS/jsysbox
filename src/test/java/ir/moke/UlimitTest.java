package ir.moke;

import ir.moke.jsysbox.system.JSystem;
import ir.moke.jsysbox.system.RLimit;
import org.junit.jupiter.api.Test;

public class UlimitTest {

    @Test
    public void getLimitsCheck() {
        int val1 = JSystem.getUlimit(RLimit.RLIMIT_NOFILE, true);
        int val2 = JSystem.getUlimit(RLimit.RLIMIT_NOFILE, false);
        System.out.println(val1 + "    " + val2);
    }

    @Test
    public void setLimitCheck() {
        JSystem.setUlimitOnPID(11095,RLimit.RLIMIT_NOFILE,20,500);
    }
}
