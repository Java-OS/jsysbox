package ir.moke.jsysbox.firewall.statement;

public class LogStatement implements Statement {

    private LogLevel level;
    private String prefix;

    public LogStatement() {
    }

    public LogStatement(LogLevel level) {
        this.level = level;
    }

    public LogStatement(LogLevel level, String prefix) {
        this.level = level;
        this.prefix = prefix;
    }

    public LogStatement(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        if (prefix != null && level != null) {
            return "log prefix \"%s\" level %s ".formatted(prefix, level.name().toLowerCase());
        } else if (prefix != null) {
            return "log prefix \"%s\" ".formatted(prefix);
        } else if (level != null) {
            return "log level " + level.name().toLowerCase();
        } else {
            return "log ";
        }
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getPrefix() {
        return prefix;
    }

    public enum LogLevel {
        LOG,
        EMERG,
        ALERT,
        CRIT,
        ERR,
        WARN,
        NOTICE,
        INFO,
        DEBUG
    }
}
