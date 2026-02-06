package net.lawliet.infinitefluidplugin.util;

public class StringUtil {
    public static String removeSuffixIfExists(String key, String suffix) {
        return key.endsWith(suffix) ? key.substring(0, key.length() - suffix.length()) : key;
    }
}
