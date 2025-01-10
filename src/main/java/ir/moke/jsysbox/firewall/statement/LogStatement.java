package ir.moke.jsysbox.firewall.statement;

import java.util.Objects;

public class LogStatement implements Statement {

    private LogLevel level;
    private String prefix;
    public LogStatement(LogLevel level) {
        this.level = level;
    }

    public LogStatement(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        if (prefix != null) {
            return "log prefix " + prefix;
        } else return "log level " + Objects.requireNonNullElse(level, LogLevel.LOG);
    }

    public enum LogLevel {
        LOG("log"),
        EMERG("log level emerg"),
        ALERT("log level alert"),
        CRIT("log level crit"),
        ERR("log level err"),
        WARN("log level warn"),
        NOTICE("log level notice"),
        INFO("log level info"),
        DEBUG("log level debug");

        private final String value;

        LogLevel(String value) {
            this.value = value;
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
