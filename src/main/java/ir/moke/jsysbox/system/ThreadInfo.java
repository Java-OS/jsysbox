package ir.moke.jsysbox.system;

import java.util.Objects;

public record ThreadInfo(long pid, long tid, String name) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ThreadInfo that = (ThreadInfo) o;
        return pid == that.pid && tid == that.tid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, tid);
    }
}
