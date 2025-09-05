package ir.moke;

import ir.moke.jsysbox.system.JSystem;
import ir.moke.jsysbox.system.ThreadInfo;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ProcessTest {

    @Test
    public void checkThread() {
        long pid = ProcessHandle.current().pid();
        Set<ThreadInfo> threads = JSystem.threads(pid);
        for (ThreadInfo thread : threads) {
            System.out.println(thread);
        }
    }
}
