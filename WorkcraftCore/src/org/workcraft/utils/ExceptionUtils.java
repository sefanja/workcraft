package org.workcraft.utils;

public class ExceptionUtils {

    public static void printCause(Throwable e) {
        e.printStackTrace();
        System.err.println("-------------" + e);
        if (e.getCause() != null) {
            printCause(e.getCause());
        }
    }

}
