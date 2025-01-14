package ir.moke.jsysbox.firewall.statement;

import ir.moke.jsysbox.JSysboxException;

public class LimitStatement implements Statement {
    private final Long rate;
    private Long burst;
    private final Boolean isOver;
    private final TimeUnit timeUnit;
    private ByteUnit rateUnit;
    private ByteUnit burstUnit;

    public LimitStatement(Long rate, TimeUnit timeUnit, Boolean isOver) {
        this.rate = rate;
        this.timeUnit = timeUnit;
        this.isOver = isOver;
    }

    public LimitStatement(Long rate, TimeUnit timeUnit, ByteUnit rateUnit, Boolean isOver) {
        this.rate = rate;
        this.timeUnit = timeUnit;
        this.rateUnit = rateUnit;
        this.isOver = isOver;
    }

    public LimitStatement(Long rate, TimeUnit timeUnit, ByteUnit rateUnit, Boolean isOver, Long burst, ByteUnit burstUnit) {
        this.rate = rate;
        this.timeUnit = timeUnit;
        this.rateUnit = rateUnit;
        this.isOver = isOver;
        this.burst = burst;
        this.burstUnit = burstUnit;
    }

    public Long getRate() {
        return rate;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public ByteUnit getRateUnit() {
        return rateUnit;
    }

    public Boolean isOver() {
        return isOver;
    }

    public Long getBurst() {
        return burst;
    }

    public Boolean getOver() {
        return isOver;
    }

    public ByteUnit getBurstUnit() {
        return burstUnit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("limit rate ");
        if (isOver) sb.append(" over ");
        if (rateUnit == null && timeUnit != null) sb.append(rate).append("/").append(timeUnit.name().toLowerCase());

        if (rateUnit != null) {
            if (timeUnit == null) throw new JSysboxException("Invalid statement, Use ByteUnit and TimeUnit together");
            sb.append(rate).append(" ").append(rateUnit.toString().toLowerCase()).append("/").append(timeUnit.name().toLowerCase());
        }

        if (burst != null) {
            sb.append(" burst ").append(burst).append(" ").append(burstUnit.name().toLowerCase());
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
