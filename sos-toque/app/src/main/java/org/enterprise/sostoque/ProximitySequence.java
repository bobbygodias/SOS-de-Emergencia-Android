package org.enterprise.sostoque;

/** Counts full far -> near -> far cycles. A pocket/continuous cover is not a gesture. */
public final class ProximitySequence {
    private boolean haveFar, near;
    private long nearAt, lastCycle, firstCycle;
    private int count;
    public static final long MAX_GAP=3000, MAX_TOTAL=15000, MIN_COVER=100, MAX_COVER=1800;
    public int update(boolean covered,long now) {
        if(count>0 && (now<lastCycle || now-lastCycle>MAX_GAP || now-firstCycle>MAX_TOTAL)) reset();
        if(!covered) {
            if(near) {
                long duration=now-nearAt;
                near=false;
                if(duration>=MIN_COVER && duration<=MAX_COVER) {
                    if(count==0) firstCycle=now;
                    if(count==0 || now-lastCycle>=250) { count++; lastCycle=now; }
                } else { count=0; }
            }
            haveFar=true;
        } else if(!near && haveFar) { near=true; nearAt=now; }
        return count;
    }
    public void reset() { haveFar=false;near=false;count=0;nearAt=lastCycle=firstCycle=0; }
}
