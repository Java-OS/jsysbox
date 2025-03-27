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
}
