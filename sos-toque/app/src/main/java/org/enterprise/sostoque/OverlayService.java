package org.enterprise.sostoque;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public final class OverlayService extends Service implements SensorEventListener {
    static final String OFF="off", TRAIN="train", ARMED="armed", TOUCH="touch";
    static OverlayService instance;
    private String mode=OFF;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ProximitySequence gesture=new ProximitySequence();
    private SensorManager sensors;
    private Sensor proximity;
    private PowerManager power;
    private PowerManager.WakeLock cpu;
    private KeyguardManager keyguard;
    private WindowManager windows;
    private Button floating;
    private TapPad pad;
    private long lastTrigger,started;
    private int previousCount;
    private boolean registered;
    private Boolean previousCovered;
    private boolean eligibleBefore;
    static String mode() { return instance==null?OFF:instance.mode; }
    static boolean running() { return instance!=null && !OFF.equals(instance.mode); }
    static Sensor sensor(Context c) {
        SensorManager sm=(SensorManager)c.getSystemService(SENSOR_SERVICE);
        if(sm==null)return null;
        Sensor s=sm.getDefaultSensor(Sensor.TYPE_PROXIMITY,true);
        return s==null?sm.getDefaultSensor(Sensor.TYPE_PROXIMITY):s;
    }
    static boolean batteryReady(Context c) {
        PowerManager p=(PowerManager)c.getSystemService(POWER_SERVICE);
        return p!=null && p.isIgnoringBatteryOptimizations(c.getPackageName());
    }
    static void start(Context c,String mode) { c.startForegroundService(new Intent(c,OverlayService.class).putExtra("mode",mode)); }
    static void refreshVisibility() {
        if(instance!=null && instance.floating!=null) {
            boolean show=SosApp.visibleActivities==0 && !TRAIN.equals(instance.mode);
            instance.floating.setVisibility(show?View.VISIBLE:View.GONE);
            if(!show && instance.pad!=null)instance.pad.reset();
        }
    }
    @Override public void onCreate() {
        super.onCreate();instance=this;
        power=(PowerManager)getSystemService(POWER_SERVICE);
        keyguard=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        windows=(WindowManager)getSystemService(WINDOW_SERVICE);
        NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch=new NotificationChannel("sos_active","SOS em funcionamento",NotificationManager.IMPORTANCE_LOW);
        ch.setDescription("Mostra quando o gesto de ajuda está ativo e permite desativar."); n.createNotificationChannel(ch);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && "stop".equals(intent.getAction())) { Prefs.get(this).edit().putString("service_mode",OFF).apply();stopSelf();return START_NOT_STICKY; }
        String requested=intent==null?Prefs.get(this).getString("service_mode",OFF):intent.getStringExtra("mode");
        if(!ARMED.equals(requested)&&!TRAIN.equals(requested)&&!TOUCH.equals(requested))requested=OFF;
        mode=requested;
        // Foreground immediately; prerequisites are checked again before registering anything.
        startForeground(1701,notification());
        releaseSensors();removeFloating();
        if(OFF.equals(mode) || !Settings.canDrawOverlays(this))return fail("Permissão para aparecer sobre outros apps ausente.");
        if(!TRAIN.equals(mode) && (!Prefs.valid(this) || checkSelfPermission(Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED))
            return fail("Contato ou permissão de chamadas ausente.");
        if(ARMED.equals(mode) && (!Prefs.get(this).getBoolean("screen_off_verified",false) || !batteryReady(this)))
            return fail("Faça o teste com tela apagada e libere a bateria antes de ativar.");
        if(!TOUCH.equals(mode)) {
            proximity=sensor(this);
            if(proximity==null)return fail("Sensor de aproximação não encontrado.");
            if(!batteryReady(this))return fail("Libere o funcionamento sem otimização de bateria.");
            if(!proximity.isWakeUpSensor()) {
                cpu=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"sostoque:proximity");cpu.setReferenceCounted(false);cpu.acquire();
            }
            registered=sensors.registerListener(this,proximity,SensorManager.SENSOR_DELAY_NORMAL,0,handler);
            if(!registered)return fail("O Android não permitiu iniciar o sensor.");
            started=SystemClock.elapsedRealtime();gesture.reset();previousCount=0;
        }
        if(!TRAIN.equals(mode)) {
            try { addFloating(); } catch(RuntimeException e) { return fail("Não foi possível mostrar o botão SOS."); }
        }
        Prefs.get(this).edit().putString("service_mode",TRAIN.equals(mode)?OFF:mode).remove("service_error").apply();
        if(TRAIN.equals(mode))handler.postDelayed(()->{
            if(TRAIN.equals(mode)) { Prefs.get(this).edit().putString("service_error","Teste encerrado após 3 minutos. Você pode tentar novamente.").apply();stopSelf(); }
        },180000);
        return TRAIN.equals(mode)?START_NOT_STICKY:START_STICKY;
    }
    private int fail(String message) {
        Prefs.get(this).edit().putString("service_error",message).putString("service_mode",OFF).apply();
        mode=OFF;stopSelf();return START_NOT_STICKY;
    }
    private Notification notification() {
        Intent main=new Intent(this,MainActivity.class);
        PendingIntent open=PendingIntent.getActivity(this,0,main,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,OverlayService.class).setAction("stop"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String text=TRAIN.equals(mode)?"TESTE sem ligação. Apague a tela e faça os cinco gestos.":ARMED.equals(mode)?"Sensor ativo: cubra e descubra o topo cinco vezes.":"Botão SOS ativo com a tela acesa.";
        return new Notification.Builder(this,"sos_active").setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(TRAIN.equals(mode)?"SOS: teste sem ligação":"SOS Toque ativo")
            .setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC).setContentIntent(open)
            .addAction(new Notification.Action.Builder(null,"Desativar",stop).build()).build();
    }
    private void addFloating() {
        floating=new Button(this);floating.setAllCaps(false);floating.setTextSize(23);floating.setTextColor(android.graphics.Color.WHITE);
        floating.setPadding(Ui.dp(this,5),0,Ui.dp(this,5),0);floating.setBackground(Ui.shape(Ui.RED,22,this));
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(Ui.dp(this,112),Ui.dp(this,120),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);
        p.gravity=(Prefs.get(this).getBoolean("left",false)?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;p.x=Ui.dp(this,8);
        pad=new TapPad(floating,Prefs.taps(this),false,true,()->{
            try { TriggerActivity.open(this,Prefs.primary(this),false,false); }
            catch(RuntimeException e) { Prefs.get(this).edit().putString("service_error","O Android impediu abrir a chamada. Abra o SOS.").apply(); }
            pad.reset();
        });
        windows.addView(floating,p);refreshVisibility();
    }
    @Override public void onSensorChanged(SensorEvent e) {
        if(!registered || !(TRAIN.equals(mode)||ARMED.equals(mode)))return;
        long now=SystemClock.elapsedRealtime();
        boolean dark=!power.isInteractive();
        // Qualification is specifically for screen OFF AND locked, not just an open activity.
        boolean locked=keyguard!=null && keyguard.isKeyguardLocked();
        boolean covered=e.values[0]<Math.min(proximity.getMaximumRange(),5f);
        Boolean oldCovered=previousCovered;previousCovered=covered;
        if(!dark || !locked || now-started<1500 || (lastTrigger>0 && now-lastTrigger<30000) || TriggerActivity.active || now-e.timestamp/1000000L>1500) {
            gesture.reset();previousCount=0;eligibleBefore=false;return;
        }
        // On-change sensors do not emit a fresh FAR event just because the screen went off.
        // Use the last observed FAR state so the user's first complete cover counts.
        if(!eligibleBefore && Boolean.FALSE.equals(oldCovered))gesture.update(false,e.timestamp/1000000L-1);
        eligibleBefore=true;
        int count=gesture.update(covered,e.timestamp/1000000L);
        if(count>previousCount)Ui.vibrate(this,45);
        previousCount=count;
        if(count<5)return;
        gesture.reset();previousCount=0;lastTrigger=now;Ui.vibrate(this,250);
        boolean training=TRAIN.equals(mode);
        try {
            // A temporary CPU lock protects the handoff from a wake-up sensor to the activity.
            PowerManager.WakeLock handoff=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"sostoque:handoff");handoff.acquire(12000);
            TriggerActivity.open(this,Prefs.primary(this),training,training);
        } catch(RuntimeException ex) {
            Prefs.get(this).edit().putString("service_error","Sensor detectado, mas o Android impediu abrir o aviso. Ainda não está validado.").apply();
        }
        if(training)stopSelf();
    }
    @Override public void onAccuracyChanged(Sensor s,int accuracy) {}
    private void releaseSensors() {
        handler.removeCallbacksAndMessages(null);
        if(sensors!=null)sensors.unregisterListener(this);registered=false;gesture.reset();previousCovered=null;eligibleBefore=false;
        if(cpu!=null && cpu.isHeld())cpu.release();cpu=null;
    }
    private void removeFloating() {
        if(pad!=null)pad.close();pad=null;
        if(floating!=null){try{windows.removeView(floating);}catch(RuntimeException ignored){}floating=null;}
    }
    @Override public void onDestroy() { releaseSensors();removeFloating();mode=OFF;if(instance==this)instance=null;stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
