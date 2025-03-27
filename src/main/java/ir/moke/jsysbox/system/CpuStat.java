package ir.moke.jsysbox.system;

public record CpuStat(String core,
                      long user,
                      long nice,
                      long system,
                      long idle,
                      long ioWait,
                      long irq,
                      long softIrq,
                      long steal,
                      long guest,
                      long guestNice) {

    public double total() {
        return user + nice + system + idle + ioWait + irq + softIrq + steal;
    }

    public double totalIdle() {
        return idle + ioWait;
    }
}
