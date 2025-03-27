package ir.moke.jsysbox.system;

public record MemoryInfo(long total,
                         long free,
                         long available,
                         long buffers,
                         long cached,
                         long swapCached,
                         long active,
                         long inactive,
                         long swapTotal,
                         long swapFree,
                         long shmem) {
}
