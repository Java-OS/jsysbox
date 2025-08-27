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

    public int getCode() {
        return code;
    }

    public static RLimit fromString(String name) {
        return switch (name.trim()) {
            case "Max cpu time" -> RLIMIT_CPU;
            case "Max file size" -> RLIMIT_FSIZE;
            case "Max data size" -> RLIMIT_DATA;
            case "Max stack size" -> RLIMIT_STACK;
            case "Max core file size" -> RLIMIT_CORE;
            case "Max resident set" -> RLIMIT_RSS;
            case "Max processes" -> RLIMIT_NPROC;
            case "Max open files" -> RLIMIT_NOFILE;
            case "Max locked memory" -> RLIMIT_MEMLOCK;
            case "Max address space" -> RLIMIT_AS;
            case "Max file locks" -> RLIMIT_LOCKS;
            case "Max pending signals" -> RLIMIT_SIGPENDING;
            case "Max msgqueue size" -> RLIMIT_MSGQUEUE;
            case "Max nice priority" -> RLIMIT_NICE;
            case "Max realtime priority" -> RLIMIT_RTPRIO;
            case "Max realtime timeout" -> RLIMIT_RTTIME;
            default -> throw new IllegalStateException("Unexpected value: " + name.trim());
        };
    }
}
