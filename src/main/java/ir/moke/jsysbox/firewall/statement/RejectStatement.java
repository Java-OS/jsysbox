package ir.moke.jsysbox.firewall.statement;

import java.util.Arrays;

public class RejectStatement implements Statement {
    private final Reason reason;

    public RejectStatement(Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return switch (reason) {
            case HOST_UNREACHABLE,
                 NET_UNREACHABLE,
                 PROT_UNREACHABLE,
                 PORT_UNREACHABLE,
                 NET_PROHIBITED,
                 HOST_PROHIBITED,
                 ADMIN_PROHIBITED,
                 NO_ROUTE, ADDR_UNREACHABLE -> "reject with icmp type %s".formatted(reason.getValue());
            case TCP_RESET -> "reject with tcp reset";
        };
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
        ADDR_UNREACHABLE("addr-unreachable"),
        TCP_RESET("tcp-reset");

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
