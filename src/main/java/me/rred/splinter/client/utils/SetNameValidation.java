package me.rred.splinter.client.utils;

public class SetNameValidation {
    public static boolean isValid(String name) {
        if (name.isEmpty() || name.length() > 20) return false;
        return name.matches("[a-zA-Z0-9 ]+");
    }
}
