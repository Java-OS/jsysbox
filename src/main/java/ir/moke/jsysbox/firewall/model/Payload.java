package ir.moke.jsysbox.firewall.model;

public class Payload {
    private Protocol protocol;
    private Field field;

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
