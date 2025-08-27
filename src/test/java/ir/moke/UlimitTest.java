package ir.moke;

import ir.moke.jsysbox.system.JSystem;
import ir.moke.jsysbox.system.RLimit;
import ir.moke.jsysbox.system.Ulimit;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UlimitTest {

    @Test
    public void checkLimits() {
        int val1 = JSystem.getUlimit(RLimit.RLIMIT_NOFILE, true);
        int val2 = JSystem.getUlimit(RLimit.RLIMIT_NOFILE, false);
        System.out.println(val1 + "    " + val2);


        List<Ulimit> allUlimits = JSystem.getAllUlimits(21205);
        for (Ulimit allUlimit : allUlimits) {
            System.out.println(allUlimit);
        }
    }
}
