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

    public double usage() {
        long total = user + nice + system + idle + ioWait + irq + softIrq + steal;
        long idle_total = idle + ioWait;
        long used = total - idle_total;
        if (total == 0) return 0;
        return (double) (100 * used / total);
    }
}
