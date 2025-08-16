package ir.moke.jsysbox.system;

public enum RLimit {
    RLIMIT_CPU(0),
    RLIMIT_FSIZE(1),
    RLIMIT_DATA(2),
    RLIMIT_STACK(3),
    RLIMIT_CORE(4),
    RLIMIT_RSS(5),
    RLIMIT_NPROC(6),
    RLIMIT_NOFILE(7),
    RLIMIT_MEMLOCK(8),
    RLIMIT_AS(9),
    RLIMIT_LOCKS(10),
    RLIMIT_SIGPENDING(11),
    RLIMIT_MSGQUEUE(12),
    RLIMIT_NICE(13),
    RLIMIT_RTPRIO(14),
    RLIMIT_RTTIME(15),
    RLIMIT_NLIMITS(16);

    private final int code;

    RLimit(int code) {
        this.code = code;
    }
}
