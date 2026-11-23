package org.enterprise.sostoque;

/** Monotonic timestamps only. One trigger per complete, uninterrupted sequence. */
public final class TapSequence {
    public static final long MAX_GAP_MS = 2500;
    public static final long MAX_TOTAL_MS = 8000;
    private final int target;
    private long first, last;
    private int count;
    private boolean fired;
    public TapSequence(int target) { this.target = target == 3 ? 3 : 5; }
    public int tap(long now) {
        if (fired) return count;
        if (count > 0 && (now < last || now - last > MAX_GAP_MS || now - first > MAX_TOTAL_MS)) reset();
        if (count == 0) first = now;
        last = now;
        count++;
        if (count >= target) fired = true;
        return count;
    }
    public boolean fired() { return fired; }
    public int target() { return target; }
    public void reset() { count = 0; fired = false; first = last = 0; }
}
