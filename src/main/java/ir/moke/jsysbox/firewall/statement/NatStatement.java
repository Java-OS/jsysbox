package ir.moke.jsysbox.firewall.statement;

import ir.moke.jsysbox.JSysboxException;

import java.util.Arrays;
import java.util.List;

public class NatStatement implements Statement {
    private final List<Flag> flags;
    private Type type = Type.MASQUERADE;
    private String address;
    private Integer port;

    public NatStatement(Type type, String address, Integer port, List<Flag> flags) {
        this.type = type;
        this.address = address;
        this.port = port;
        this.flags = flags;
    }

    public NatStatement(Type type, Integer port, List<Flag> flags) {
        this.type = type;
        this.port = port;
        this.flags = flags;
    }

    /**
     * @param type    type of nat {@link Type}
     * @param address target address
     * @param flags   nat flags {@link Flag}
     */
    public NatStatement(Type type, String address, List<Flag> flags) {
        this.type = type;
        this.address = address;
        this.flags = flags;
    }

    /**
     * This constructor usable for masquerade
     *
     * @param flags nat flags {@link Flag}
     */
    public NatStatement(List<Flag> flags) {
        this.flags = flags;
    }

    public Type getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public List<Flag> getFlag() {
        return flags;
    }

    @Override
    public String toString() {
        String result = "";
        switch (type) {
            case DNAT -> {
                if (port != null) {
                    result = "dnat to %s:%s".formatted(address, port);
                } else {
                    result = "dnat to %s".formatted(address);
                }
            }
            case SNAT -> {
                if (port != null) {
                    result = "snat to %s:%s".formatted(address, port);
                } else {
                    result = "snat to %s".formatted(address);
                }
            }
            case REDIRECT -> {
                if (port == null) throw new JSysboxException("Port can not be empty");
                result = "redirect to %s".formatted(port);
            }
            case MASQUERADE -> result = "masquerade";
        }
        if (flags != null) {
            String flagStr = String.join(",", flags.stream().map(Flag::getValue).toList());
            return result + " " + flagStr;
        }
        return result;
    }

    public enum Type {
        SNAT,
        DNAT,
        REDIRECT,
        MASQUERADE
    }

    public enum Flag {
        RANDOM("random"),
        PERSISTENT("persistent"),
        FULLY_RANDOM("fully-random");

        private final String value;

        Flag(String value) {
            this.value = value;
        }

        public static Flag fromValue(String value) {
            return Arrays.stream(Flag.class.getEnumConstants())
                    .filter(item -> item.getValue().equals(value))
                    .findFirst()
                    .orElse(null);
        }

        public String getValue() {
            return value;
        }
    }
}
