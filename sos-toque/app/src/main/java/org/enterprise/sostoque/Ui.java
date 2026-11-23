package org.enterprise.sostoque;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class Ui {
    static final int BG=Color.rgb(17,24,32), CARD=Color.rgb(37,49,63), RED=Color.rgb(172,23,48), AMBER=Color.rgb(255,213,154);
    static int dp(Context c, float x) { return Math.round(x*c.getResources().getDisplayMetrics().density); }
    static GradientDrawable shape(int color, int radius, Context c) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(c,radius)); return d;
    }
    static LinearLayout screen(Activity a) {
        ScrollView s=new ScrollView(a); s.setFillViewport(true); s.setBackgroundColor(BG);
        LinearLayout l=new LinearLayout(a); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(a,22),dp(a,18),dp(a,22),dp(a,24));
        s.addView(l,new ScrollView.LayoutParams(-1,-2));
        if(Build.VERSION.SDK_INT>=30) a.getWindow().setDecorFitsSystemWindows(false);
        s.setOnApplyWindowInsetsListener((v,in)->{
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets i=in.getInsets(android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout() | android.view.WindowInsets.Type.ime());
                v.setPadding(i.left,i.top,i.right,i.bottom);
            } else v.setPadding(in.getSystemWindowInsetLeft(),in.getSystemWindowInsetTop(),in.getSystemWindowInsetRight(),in.getSystemWindowInsetBottom());
            return in;
        });
        a.setContentView(s); s.requestApplyInsets(); return l;
    }
    static TextView text(LinearLayout l,String value,int size,int color) {
        TextView t=new TextView(l.getContext()); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(0,dp(l.getContext(),7),0,dp(l.getContext(),7));
        l.addView(t,new LinearLayout.LayoutParams(-1,-2)); return t;
    }
    static void title(LinearLayout l,String value) { text(l,value,32,Color.WHITE).setTypeface(null,Typeface.BOLD); }
    static Button button(LinearLayout l,String title,int color,Runnable r) {
        Button b=new Button(l.getContext()); b.setText(title); b.setTextSize(19); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setGravity(Gravity.CENTER); b.setMinHeight(dp(l.getContext(),62));
        b.setPadding(dp(l.getContext(),14),dp(l.getContext(),15),dp(l.getContext(),14),dp(l.getContext(),15));
        b.setBackground(shape(color,16,l.getContext()));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(l.getContext(),12);
        l.addView(b,p); if(r!=null) b.setOnClickListener(v->r.run()); return b;
    }
    static void vibrate(Context c,long ms) {
        Vibrator v=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
        if(v!=null && v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));
    }
    static void awake(Activity a) {
        a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=27) a.setShowWhenLocked(true);
        else a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }
}
