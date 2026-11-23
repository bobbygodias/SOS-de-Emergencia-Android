package org.enterprise.sostoque;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Button;

/** A real clickable Button also supports TalkBack/keyboard activation. */
final class TapPad {
    private final Button button;
    private final TapSequence sequence;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final boolean training;
    private final boolean compact;
    private final Runnable complete;
    private final Runnable timeout=()->reset();
    TapPad(Button button,int taps,boolean training,boolean compact,Runnable complete) {
        this.button=button; this.training=training; this.compact=compact; this.complete=complete;
        sequence=new TapSequence(taps); reset();
        button.setOnClickListener(v->{
            if(sequence.fired()) return;
            int n=sequence.tap(SystemClock.elapsedRealtime());
            Ui.vibrate(button.getContext(),35);
            handler.removeCallbacks(timeout);
            button.setText((training?"TREINO":"SOS")+"\n"+n+" de "+sequence.target());
            button.setContentDescription((training?"Treino, ":"SOS, ")+n+" de "+sequence.target()+" toques");
            if(sequence.fired()) {
                Ui.vibrate(button.getContext(),150);
                handler.postDelayed(complete,70);
            } else handler.postDelayed(timeout,TapSequence.MAX_GAP_MS+1);
        });
    }
    void reset() {
        handler.removeCallbacksAndMessages(null); sequence.reset();
        button.setText((training?"TREINO":"SOS")+"\n"+(compact?sequence.target()+" toques":"Toque "+sequence.target()+" vezes aqui"));
        button.setContentDescription((training?"Treino sem ligação. ":"Pedir ajuda. ")+"Toque "+sequence.target()+" vezes neste botão.");
    }
    void close() { handler.removeCallbacksAndMessages(null); sequence.reset(); }
}
