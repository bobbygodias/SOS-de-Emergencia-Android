package org.enterprise.sostoque;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

public final class ReadyActivity extends Activity {
    private TapPad pad;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b); Ui.awake(this);
        boolean training=getIntent().getBooleanExtra("training",false);
        LinearLayout l=Ui.screen(this); Ui.title(l,training?"Treino de toques":"Tela SOS pronta");
        Ui.text(l,training?"Nenhuma ligação será feita.":"Para chamar: "+Prefs.name(this,Prefs.primary(this)),21,Ui.AMBER);
        Button button=Ui.button(l,"",training?Ui.CARD:Ui.RED,null); button.setTextSize(30);
        button.setMinHeight(Ui.dp(this,300));
        pad=new TapPad(button,Prefs.taps(this),training,false,()->{
            TriggerActivity.open(this,Prefs.primary(this),training,false); pad.reset();
        });
        Ui.text(l,"A tela permanece acesa. Este botão pode aparecer sobre o bloqueio, mas não recebe toques depois que a tela apagar.",17,Color.LTGRAY);
        Ui.button(l,"Voltar",Ui.CARD,()->finish());
    }
    @Override public void onPause() { super.onPause(); if(pad!=null)pad.reset(); }
    @Override public void onDestroy() { if(pad!=null)pad.close(); super.onDestroy(); }
}
