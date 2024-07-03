package ir.moke.jsysbox.firewall.statement;

import ir.moke.jsysbox.JSysboxException;

public class LimitStatement implements Statement {
    private final long value;
    private final boolean isOver;
    private final TimeUnit timeUnit;
    private ByteUnit byteUnit;

    public LimitStatement(long value, TimeUnit timeUnit, boolean isOver) {
        this.value = value;
        this.timeUnit = timeUnit;
        this.isOver = isOver;
    }

    public LimitStatement(long value, TimeUnit timeUnit, ByteUnit byteUnit, boolean isOver) {
        this.value = value;
        this.timeUnit = timeUnit;
        this.byteUnit = byteUnit;
        this.isOver = isOver;
    }

    public long getValue() {
        return value;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ByteUnit getByteUnit() {
        return byteUnit;
    }

    public boolean isOver() {
        return isOver;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("limit rate ");
        if (isOver) sb.append(" over ");
        if (byteUnit == null && timeUnit != null) sb.append(value).append("/").append(timeUnit.name().toLowerCase());

        if (byteUnit != null) {
            if (timeUnit == null) throw new JSysboxException("Invalid statement, Use ByteUnit and TimeUnit together");
            sb.append(value).append(" ").append(byteUnit.toString().toLowerCase()).append("/").append(timeUnit.toString().toLowerCase());
        }

        return sb.toString();
    }

    public enum TimeUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY
    }

    public enum ByteUnit {
        BYTES,
        KBYTES,
        MBYTES
    }
}
