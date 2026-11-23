package org.enterprise.sostoque;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private boolean editing;
    private EditText[] names,numbers;
    private RadioGroup principal,taps;
    private CheckBox voice,left;
    private TapPad pad;
    private TextView serviceStatus;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int picking;
    private final Runnable refresh=new Runnable(){public void run(){if(!editing && serviceStatus!=null){String value=status();if(!value.contentEquals(serviceStatus.getText()))serviceStatus.setText(value);}handler.postDelayed(this,1000);}};
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if(b!=null && b.getBoolean("editing",false)) {
            settings();picking=b.getInt("picking",0);
            for(int i=0;i<3;i++){names[i].setText(b.getString("draft_name_"+i,""));numbers[i].setText(b.getString("draft_number_"+i,""));}
            principal.check(100+b.getInt("draft_primary",0));taps.check(b.getInt("draft_taps",5));
            voice.setChecked(b.getBoolean("draft_voice",true));left.setChecked(b.getBoolean("draft_left",false));
        } else home();
    }
    @Override public void onResume(){super.onResume();if(!editing)home();handler.removeCallbacks(refresh);handler.post(refresh);}
    @Override public void onPause(){handler.removeCallbacks(refresh);if(pad!=null)pad.reset();super.onPause();}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);if(pad!=null)pad.close();super.onDestroy();}
    private void message(String title,String value){new AlertDialog.Builder(this).setTitle(title).setMessage(value).setPositiveButton("Entendi",null).show();}
    private boolean callGranted(){return checkSelfPermission(Manifest.permission.CALL_PHONE)==PackageManager.PERMISSION_GRANTED;}
    private String status(){
        String error=Prefs.get(this).getString("service_error","");
        String mode=OverlayService.mode();
        if(OverlayService.ARMED.equals(mode))return "ATIVO · Sensor para tela apagada e bloqueada.\nCubra e descubra o topo cinco vezes.";
        if(OverlayService.TRAIN.equals(mode))return "TESTE EM ANDAMENTO · Nenhuma ligação.\nApague a tela e cubra/descubra o topo cinco vezes.";
        if(OverlayService.TOUCH.equals(mode))return "Botão flutuante ativo. Sensor para tela apagada DESATIVADO.";
        return "SOS em segundo plano DESATIVADO."+(error.isEmpty()?"":"\n"+error);
    }
    private void home(){
        editing=false;if(pad!=null)pad.close();pad=null;
        LinearLayout l=Ui.screen(this);Ui.title(l,"SOS Toque");
        Ui.text(l,"Ajuda sem apertar o botão lateral",21,Ui.AMBER);
        serviceStatus=Ui.text(l,status(),19,Color.WHITE);
        Ui.text(l,Prefs.valid(this)?"Contato principal: "+Prefs.name(this,Prefs.primary(this)):"Primeiro, escolha quem deve receber a ligação.",20,Color.WHITE);
        Ui.button(l,Prefs.valid(this)?"Contatos e ajustes":"1. Escolher contato da agenda",Ui.CARD,()->settings());
        Sensor s=OverlayService.sensor(this);
        if(s==null)Ui.text(l,"Sensor de aproximação não encontrado. O acionamento com tela apagada não está disponível neste aparelho.",19,Ui.AMBER);
        else {
            Ui.text(l,"Com a tela apagada: cubra e descubra a parte superior do celular com a mão cinco vezes. Não é tocar no meio da tela.",19,Color.WHITE);
            Ui.button(l,"2. Testar sensor sem ligar",Ui.CARD,()->beginSensor(true));
            boolean tested=Prefs.get(this).getBoolean("screen_off_verified",false);
            Ui.text(l,tested?"Teste com tela apagada: passou. Falta conferir uma chamada real ao contato.":"O modo real só libera depois do teste no seu aparelho.",17,Ui.AMBER);
            Ui.button(l,"3. ATIVAR SOS com tela apagada",Ui.RED,()->beginSensor(false));
        }
        if(OverlayService.running())Ui.button(l,"DESATIVAR SOS",Ui.CARD,()->{
            Prefs.get(this).edit().putString("service_mode",OverlayService.OFF).apply();stopService(new Intent(this,OverlayService.class));handler.postDelayed(()->home(),150);
        });
        Ui.text(l,"Também disponível com a tela acesa",20,Ui.AMBER);
        Button touch=Ui.button(l,"",Ui.RED,null);touch.setMinHeight(Ui.dp(this,150));touch.setTextSize(27);
        pad=new TapPad(touch,Prefs.taps(this),false,false,()->{
            if(readyToCall())TriggerActivity.open(this,Prefs.primary(this),false,false);pad.reset();
        });
        Ui.button(l,"Treinar toques sem ligar",Ui.CARD,()->startActivity(new Intent(this,ReadyActivity.class).putExtra("training",true)));
        Ui.button(l,"Manter tela SOS acesa",Ui.CARD,()->{if(readyToCall())startActivity(new Intent(this,ReadyActivity.class));});
        Ui.button(l,"Somente botão sobre outros apps",Ui.CARD,()->{
            if(readyToCall() && overlayReady()){OverlayService.start(this,OverlayService.TOUCH);handler.postDelayed(()->home(),250);}
        });
        Ui.text(l,"Se reiniciar ou forçar a parada do aplicativo, abra-o e ative novamente. Mantenha o SOS original do celular disponível.",17,Color.LTGRAY);
    }
    private boolean readyToCall(){
        if(!Prefs.valid(this)){settings();return false;}
        if(!callGranted()){requestPermissions(new String[]{Manifest.permission.CALL_PHONE},21);return false;}
        return true;
    }
    private boolean overlayReady(){
        if(Settings.canDrawOverlays(this))return true;
        new AlertDialog.Builder(this).setTitle("Permitir aviso sobre o bloqueio")
            .setMessage("Na próxima tela, selecione SOS Toque e permita aparecer sobre outros aplicativos. Volte e toque novamente no teste.")
            .setPositiveButton("Abrir permissão",(d,w)->openSettings(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))))
            .setNegativeButton("Voltar",null).show();return false;
    }
    private void beginSensor(boolean training){
        if(OverlayService.sensor(this)==null){message("Sem sensor","Este aparelho não disponibilizou o sensor de aproximação.");return;}
        if(!training && !readyToCall())return;
        if(!overlayReady())return;
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},22);return;}
        if(!OverlayService.batteryReady(this)){
            new AlertDialog.Builder(this).setTitle("Manter SOS disponível com a tela apagada")
                .setMessage("Permita que o SOS funcione sem otimização de bateria. Isso é necessário para este modo e pode aumentar o consumo. Depois volte e toque novamente no teste.")
                .setPositiveButton("Permitir",(d,w)->openSettings(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,Uri.parse("package:"+getPackageName()))))
                .setNegativeButton("Voltar",null).show();return;
        }
        if(!training && !Prefs.get(this).getBoolean("screen_off_verified",false)){message("Faça o teste primeiro","Use Testar sensor sem ligar. O teste precisa terminar com a tela apagada e bloqueada.");return;}
        if(!training && Numbers.requiresDialer(Prefs.number(this,Prefs.primary(this)))){message("Escolha um contato pessoal","Para ligar sem confirmação, selecione um telefone completo com DDD. Números curtos de serviços públicos exigem o discador do sistema.");return;}
        if(training){
            new AlertDialog.Builder(this).setTitle("Teste sem nenhuma ligação")
                .setMessage("Depois de iniciar, apague e bloqueie a tela. Com o telefone parado, cubra e descubra o sensor junto ao alto-falante superior cinco vezes. Deixe a mão sobre ele por cerca de meio segundo a cada vez. Cada gesto reconhecido vibra. Não deixe mais de três segundos entre gestos.\n\nSe funcionar, a tela acende e mostra Sensor respondeu. O teste termina após três minutos.")
                .setPositiveButton("Iniciar teste",(d,w)->{OverlayService.start(this,OverlayService.TRAIN);handler.postDelayed(()->home(),250);})
                .setNegativeButton("Voltar",null).show();
        } else {
            new AlertDialog.Builder(this).setTitle("Ativar chamadas reais")
                .setMessage("Cinco gestos com tela apagada e bloqueada iniciarão uma contagem de cinco segundos e pedirão uma ligação para "+Prefs.name(this,Prefs.primary(this))+". Combine com esse contato e faça um teste real agora. Se houver dois chips, defina o chip padrão de chamadas no Android.")
                .setPositiveButton("ATIVAR",(d,w)->{OverlayService.start(this,OverlayService.ARMED);handler.postDelayed(()->home(),250);})
                .setNegativeButton("Voltar",null).show();
        }
    }
    private void openSettings(Intent i){try{startActivity(i);}catch(RuntimeException e){message("Abra as configurações","Abra Configurações, Aplicativos, SOS Toque e ajuste a permissão indicada.");}}
    private EditText input(LinearLayout l,String hint,String value,boolean phone){
        EditText e=new EditText(this);e.setHint(hint);e.setText(value);e.setTextSize(20);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.LTGRAY);
        e.setInputType(phone?InputType.TYPE_CLASS_PHONE:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        e.setSingleLine(true);e.setMinHeight(Ui.dp(this,58));l.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;
    }
    private void settings(){
        editing=true;if(pad!=null)pad.close();pad=null;LinearLayout l=Ui.screen(this);Ui.title(l,"Contatos e ajustes");
        Ui.text(l,"Escolha aqui o mesmo contato de emergência da agenda. O Android não fornece ao app a lista privada de contatos do SOS do sistema.",18,Color.LTGRAY);
        names=new EditText[3];numbers=new EditText[3];
        for(int i=0;i<3;i++){
            final int slot=i;Ui.text(l,"Contato "+(i+1)+(i==0?"":" · opcional"),22,Ui.AMBER);
            names[i]=input(l,"Nome",Prefs.get(this).getString("name_"+i,""),false);
            numbers[i]=input(l,"Telefone com DDD",Prefs.get(this).getString("number_"+i,""),true);
            Ui.button(l,"Escolher da agenda",Ui.CARD,()->{picking=slot;try{startActivityForResult(new Intent(Intent.ACTION_PICK,ContactsContract.CommonDataKinds.Phone.CONTENT_URI),40);}catch(RuntimeException e){message("Digite o contato","Não foi possível abrir a agenda. Digite o nome e o telefone acima.");}});
        }
        Ui.text(l,"Quem o gesto deve chamar?",22,Ui.AMBER);principal=new RadioGroup(this);
        for(int i=0;i<3;i++){RadioButton b=new RadioButton(this);b.setId(100+i);b.setText("Contato "+(i+1));b.setTextSize(20);b.setMinHeight(Ui.dp(this,52));principal.addView(b);}
        principal.check(100+Prefs.primary(this));l.addView(principal);
        Ui.text(l,"Toques nos botões com tela acesa",22,Ui.AMBER);taps=new RadioGroup(this);
        for(int n:new int[]{5,3}){RadioButton b=new RadioButton(this);b.setId(n);b.setText(n+" toques");b.setTextSize(20);b.setMinHeight(Ui.dp(this,52));taps.addView(b);}taps.check(Prefs.taps(this));l.addView(taps);
        Ui.text(l,"O sensor com tela apagada usa sempre cinco gestos completos.",17,Color.LTGRAY);
        voice=new CheckBox(this);voice.setText("Aviso de voz na contagem");voice.setTextSize(20);voice.setChecked(Prefs.get(this).getBoolean("voice",true));l.addView(voice);
        left=new CheckBox(this);left.setText("Botão flutuante do lado esquerdo");left.setTextSize(20);left.setChecked(Prefs.get(this).getBoolean("left",false));l.addView(left);
        Ui.button(l,"SALVAR",Ui.RED,()->save());Ui.button(l,"Voltar sem salvar",Ui.CARD,()->home());
        Ui.text(l,"Sem internet, anúncios ou envio de dados. Chamadas usam o seu chip. O viva-voz é solicitado ao Android. Nenhuma ligação é feita para os contatos de reserva automaticamente.",17,Color.LTGRAY);
    }
    private void save(){
        int selected=principal.getCheckedRadioButtonId()-100;
        String[] normalized=new String[3];
        for(int i=0;i<3;i++){
            String raw=numbers[i].getText().toString().trim();normalized[i]=Numbers.normalize(raw);
            if(!raw.isEmpty() && normalized[i].isEmpty()){numbers[i].setError("Use apenas um telefone, com DDD. Sem ramal ou comandos.");numbers[i].requestFocus();return;}
        }
        if(selected<0 || selected>2 || normalized[selected].isEmpty()){message("Falta o telefone principal","Preencha o telefone do contato selecionado como principal.");return;}
        SharedPreferences.Editor e=Prefs.get(this).edit();
        for(int i=0;i<3;i++)e.putString("name_"+i,names[i].getText().toString().trim()).putString("number_"+i,normalized[i]);
        e.putInt("primary",selected).putInt("taps",taps.getCheckedRadioButtonId()==3?3:5).putBoolean("voice",voice.isChecked()).putBoolean("left",left.isChecked()).apply();
        // Editing settings is explicit: disarm to avoid silently changing a live recipient.
        Prefs.get(this).edit().putString("service_mode",OverlayService.OFF).apply();stopService(new Intent(this,OverlayService.class));
        home();message("Contatos salvos","Ative o SOS no início do aplicativo para usar o contato escolhido.");
    }
    @Override public void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==40 && result==RESULT_OK && data!=null && data.getData()!=null && editing){
            try(Cursor c=getContentResolver().query(data.getData(),new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER},null,null,null)){
                if(c!=null && c.moveToFirst()){names[picking].setText(c.getString(0));numbers[picking].setText(c.getString(1));}
            }catch(RuntimeException e){message("Digite o contato","A agenda não compartilhou esse número. Digite o nome e o telefone.");}
        }
    }
    @Override public void onRequestPermissionsResult(int request,String[] p,int[] granted){
        super.onRequestPermissionsResult(request,p,granted);
        if(granted.length==0 || granted[0]!=PackageManager.PERMISSION_GRANTED)message("Permissão não concedida","O modo que depende desta permissão ainda não está pronto. Você pode autorizá-la em Configurações, Aplicativos, SOS Toque, Permissões.");
        else message("Permissão concedida","Toque novamente no teste ou na ativação para continuar.");
        if(!editing)home();
    }
    @Override public void onSaveInstanceState(Bundle b){
        b.putBoolean("editing",editing);b.putInt("picking",picking);
        if(editing){for(int i=0;i<3;i++){b.putString("draft_name_"+i,names[i].getText().toString());b.putString("draft_number_"+i,numbers[i].getText().toString());}b.putInt("draft_primary",principal.getCheckedRadioButtonId()-100);b.putInt("draft_taps",taps.getCheckedRadioButtonId());b.putBoolean("draft_voice",voice.isChecked());b.putBoolean("draft_left",left.isChecked());}
        super.onSaveInstanceState(b);
    }
    @Override public void onBackPressed(){if(editing)home();else super.onBackPressed();}
}
