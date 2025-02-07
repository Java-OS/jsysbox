package ir.moke.jsysbox.firewall.statement;

import java.util.Arrays;

public class RejectStatement implements Statement {

    private final Type type;
    private final Reason reason;

    public RejectStatement(Type type, Reason reason) {
        this.type = type;
        this.reason = reason;
    }

    public Type getType() {
        return type;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return switch (type) {
            case TCP_RESET -> "reject with tcp reset";
            case ICMP -> "reject with icmp type %s".formatted(reason.getValue());
            case ICMPV6 -> "reject with icmpv6 type %s".formatted(reason.getValue());
            case ICMPX -> "reject with icmpx type %s".formatted(reason.getValue());
        };
    }

    public enum Type {
        TCP_RESET("tcp reset"),
        ICMP("icmp"),
        ICMPV6("icmpv6"),
        ICMPX("icmpx");
        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            return Arrays.stream(Type.class.getEnumConstants())
                    .filter(item -> item.getValue().equals(value))
                    .findFirst()
                    .orElse(null);
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public enum Reason {
        HOST_UNREACHABLE("host-unreachable"),
        NET_UNREACHABLE("net-unreachable"),
        PROT_UNREACHABLE("prot-unreachable"),
        PORT_UNREACHABLE("port-unreachable"),
        NET_PROHIBITED("net-prohibited"),
        HOST_PROHIBITED("host-prohibited"),
        ADMIN_PROHIBITED("admin-prohibited"),
        NO_ROUTE("no-route"),
        ADDR_UNREACHABLE("addr-unreachable");

        private final String value;

        Reason(String value) {
            this.value = value;
        }

        public static Reason fromValue(String value) {
            return Arrays.stream(Reason.class.getEnumConstants())
                    .filter(item -> item.getValue().equals(value))
                    .findFirst()
                    .orElse(null);
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
