package ir.moke.jsysbox.firewall.statement;

public enum LogStatement implements Statement {
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

    LogStatement(String value) {
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
