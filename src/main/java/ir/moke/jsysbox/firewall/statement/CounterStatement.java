package ir.moke.jsysbox.firewall.statement;

public class CounterStatement implements Statement {

    private Long packets;
    private Long bytes;

    public CounterStatement() {
    }

    public CounterStatement(Long packets, Long bytes) {
        this.packets = packets;
        this.bytes = bytes;
    }

    public Long getPackets() {
        return packets;
    }

    public Long getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "counter";
    }
}
