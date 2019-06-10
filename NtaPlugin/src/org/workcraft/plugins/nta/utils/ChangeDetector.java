package org.workcraft.plugins.nta.utils;

public class ChangeDetector {
    public static boolean hasChanged(String str1, String str2) {
        return (str1 != null || str2 != null) && (str1 == null || !str1.equals(str2));
    }

    public static boolean hasChanged(boolean bool1, boolean bool2) {
        return bool1 != bool2;
    }

    public static boolean hasChanged(int int1, int int2) {
        return int1 != int2;
    }
}
