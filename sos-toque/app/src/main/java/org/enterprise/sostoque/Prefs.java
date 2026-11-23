package org.enterprise.sostoque;

import android.content.Context;
import android.content.SharedPreferences;
final class Prefs {
    static SharedPreferences get(Context c) { return c.getSharedPreferences("sos_private", Context.MODE_PRIVATE); }
    static String number(Context c, int i) { return Numbers.normalize(get(c).getString("number_"+i, "")); }
    static String name(Context c, int i) {
        String s = get(c).getString("name_"+i, "").trim();
        return s.isEmpty() ? "Contato "+(i+1) : s;
    }
    static int primary(Context c) { return Math.max(0, Math.min(2,get(c).getInt("primary", 0))); }
    static int taps(Context c) { return get(c).getInt("taps", 5) == 3 ? 3 : 5; }
    static boolean valid(Context c) { return !number(c,primary(c)).isEmpty(); }
}
