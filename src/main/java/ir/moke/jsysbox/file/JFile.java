package ir.moke.jsysbox.file;

import ir.moke.jsysbox.JniNativeLoader;

public class JFile {

    static {
        JniNativeLoader.load("jfile");
    }

    public native static String mime(String filePath);
}
