package org.enterprise.sostoque;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public final class TriggerActivity extends Activity {
    static boolean active;
    private boolean owns,completed,training,sensorTest;
    private int contact;
    private String number,name;
    private long deadline;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView counter;
    private TextToSpeech speech;
    static void open(Context c,int contact,boolean training,boolean sensorTest) {
        if(active) return;
        Intent i=new Intent(c,TriggerActivity.class).putExtra("contact",contact).putExtra("training",training).putExtra("sensor_test",sensorTest);
        if(!(c instanceof Activity))i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(i);
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(active) { finish();return; }
        active=true;owns=true;
        Ui.awake(this);
        if(Build.VERSION.SDK_INT>=27)setTurnScreenOn(true);
        else getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        contact=Math.max(0,Math.min(2,getIntent().getIntExtra("contact",Prefs.primary(this))));
        training=getIntent().getBooleanExtra("training",false);
        sensorTest=getIntent().getBooleanExtra("sensor_test",false);
        number=Prefs.number(this,contact); name=Prefs.name(this,contact);
        // A process recreation must never replay a pending call.
        if(state!=null) { completed=true; result("Ação interrompida", "Toque novamente no SOS se ainda precisar chamar.");return; }
        if(!training && number.isEmpty()) { completed=true;result("Contato não configurado","Abra Contatos e ajustes para escolher um telefone.");return; }
        if(sensorTest) {
            completed=true; result("Sensor respondeu", "Os cinco gestos foram detectados com a tela apagada e bloqueada. Nenhuma ligação foi feita. Agora você pode ativar o modo real no início do app.");
            speak("Teste concluído. O sensor respondeu com a tela apagada. Nenhuma ligação foi feita.");
            return;
        }
        LinearLayout l=Ui.screen(this); Ui.title(l,training?"TREINO SEM LIGAÇÃO":"Pedido de ajuda");
        Ui.text(l,training?"Simulando a chamada.":"Chamar "+name,26,Color.WHITE);
        counter=Ui.text(l,"",64,Ui.AMBER); counter.setGravity(android.view.Gravity.CENTER);
        Ui.button(l,"CANCELAR",Ui.CARD,()->{completed=true;handler.removeCallbacksAndMessages(null);finish();});
        Ui.text(l,"A ligação será solicitada ao fim da contagem.",18,Color.LTGRAY);
        deadline=SystemClock.elapsedRealtime()+5000;
        speak(training?"Treino. Nenhuma ligação será feita.":"SOS. Chamando "+name+" em cinco segundos.");
        handler.post(tick);
    }
    private final Runnable tick=new Runnable() { public void run() {
        if(completed)return;
        long left=deadline-SystemClock.elapsedRealtime();
        if(left<=0) { dispatch();return; }
        counter.setText(String.valueOf((left+999)/1000));
        handler.postDelayed(this,100);
    }};
    private void dispatch() {
        if(completed)return;
        completed=true;handler.removeCallbacksAndMessages(null);
        if(training) { Ui.vibrate(this,250);result("Treino concluído","O gesto e a contagem funcionaram. Nenhuma ligação foi feita.");return; }
        if(speech!=null)speech.stop();
        if(checkSelfPermission(Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED) {
            result("Permissão necessária","O SOS não iniciou a ligação. Permita chamadas nas configurações do aplicativo.");return;
        }
        Uri tel=Uri.fromParts("tel",number,null);
        try {
            if(Numbers.requiresDialer(number) || PhoneNumberUtils.isEmergencyNumber(number)) {
                result("Confirme no telefone","Este número precisa de confirmação no discador do Android.");
                startActivity(new Intent(Intent.ACTION_DIAL,tel));
            } else {
                TelecomManager telecom=(TelecomManager)getSystemService(TELECOM_SERVICE);
                if(telecom==null)throw new IllegalStateException("Sem serviço telefônico");
                Bundle extras=new Bundle(); extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE,true);
                result("Chamada solicitada","Confira a ligação na tela do telefone. O chip pode pedir confirmação. Este app não confirma se alguém atendeu.");
                telecom.placeCall(tel,extras);
            }
        } catch(RuntimeException e) {
            result("Não foi possível iniciar","O Android recusou a chamada. Use o discador ou escolha outro contato.");
        }
    }
    private void result(String heading,String body) {
        LinearLayout l=Ui.screen(this);Ui.title(l,heading);Ui.text(l,body,21,Color.WHITE);
        if(!training) for(int i=0;i<3;i++) {
            final int selected=i;
            if(!Prefs.number(this,i).isEmpty())Ui.button(l,"Chamar "+Prefs.name(this,i),Ui.RED,()->{
                // New explicit user gesture, with a new countdown; never an automatic retry.
                active=false;owns=false;finish();open(this,selected,false,false);
            });
        }
        Ui.button(l,"Voltar ao início",Ui.CARD,()->{
            Intent i=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(i);finish();
        });
        Ui.button(l,"Fechar",Ui.CARD,()->finish());
    }
    private void speak(String value) {
        if(!Prefs.get(this).getBoolean("voice",true))return;
        speech=new TextToSpeech(this,status->{
            if(status==TextToSpeech.SUCCESS && speech!=null && (!completed || sensorTest)) {
                speech.setLanguage(new Locale("pt","BR"));
                android.speech.tts.Voice local=null;
                if(speech.getVoices()!=null)for(android.speech.tts.Voice v:speech.getVoices()) {
                    if(!v.isNetworkConnectionRequired() && "pt".equals(v.getLocale().getLanguage())) {
                        local=v;if("BR".equals(v.getLocale().getCountry()))break;
                    }
                }
                if(local!=null){speech.setVoice(local);speech.speak(value,TextToSpeech.QUEUE_FLUSH,null,"sos");}
            }
        });
    }
    @Override public void onPause() {
        super.onPause();
        if(!completed){completed=true;handler.removeCallbacksAndMessages(null);finish();}
        // A completed screen can remain behind the phone app; it must not block later SOS gestures.
        if(owns){active=false;owns=false;}
    }
    @Override public void onResume() { super.onResume(); if(sensorTest && owns)Prefs.get(this).edit().putBoolean("screen_off_verified",true).apply(); }
    @Override public void onSaveInstanceState(Bundle b) { b.putBoolean("do_not_repeat",true);super.onSaveInstanceState(b); }
    @Override public void onDestroy() { handler.removeCallbacksAndMessages(null);if(speech!=null){speech.stop();speech.shutdown();}if(owns)active=false;super.onDestroy(); }
}
