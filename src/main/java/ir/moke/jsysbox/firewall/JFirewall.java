package ir.moke.jsysbox.firewall;

import ir.moke.jsysbox.JniNativeLoader;

public class JFirewall {

    static {
        JniNativeLoader.load("jfirewall");
    }

    public native static void restore(String file);

    public native static String export();
}
