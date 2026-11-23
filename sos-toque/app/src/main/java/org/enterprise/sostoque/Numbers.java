package org.enterprise.sostoque;

public final class Numbers {
    private Numbers() {}
    /** Rejects USSD, post-dial commands and URI injection. Does not guess country codes. */
    public static String normalize(String input) {
        if (input == null || !input.matches("[+0-9 ()\\-.\\s]+")) return "";
        String n = input.replaceAll("[ ()\\-.\\s]", "");
        return n.matches("\\+?[0-9]{3,15}") ? n : "";
    }
    public static boolean requiresDialer(String number) {
        int len = number.startsWith("+") ? number.length() - 1 : number.length();
        return len < 8;
    }
}
