package ir.moke.jsysbox.network;

import java.util.Arrays;

public record Netstat(int index,
                      Protocol protocol,
                      String localAddress,
                      int localPort,
                      String remoteAddress,
                      int remotePort,
                      Status status,
                      Integer pid,
                      String cmd) {
    public enum Protocol {
        TCP,
        UDP,
        UDP_LITE
    }

    public enum Status {
        ESTABLISHED(0x01),
        SYN_SENT(0x02),
        SYN_RECV(0x03),
        FIN_WAIT1(0x04),
        FIN_WAIT2(0x05),
        TIME_WAIT(0x06),
        CLOSE(0x07),
        CLOSE_WAIT(0x08),
        LAST_ACK(0x09),
        LISTEN(0x0A),
        CLOSING(0x0B),
        UNKNOWN(0x0C);

        private final int hex;

        Status(int hex) {
            this.hex = hex;
        }

        public static Status getFromHex(int hex) {
            return Arrays.stream(Status.class.getEnumConstants())
                    .filter(item -> item.hex == hex)
                    .findFirst()
                    .orElse(null);
        }
    }
}
