package com.chizhu.wififloat;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import rikka.shizuku.Shizuku;

public class FloatService extends Service {
    private WindowManager wm; private TextView bubble; private WindowManager.LayoutParams params;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean busy = false;
    private IWifiControl remote;
    private final Shizuku.UserServiceArgs serviceArgs = new Shizuku.UserServiceArgs(
        new android.content.ComponentName("com.chizhu.wififloat", WifiUserService.class.getName()))
        .daemon(false).processNameSuffix("wifi").debuggable(false).version(1);
    private final android.content.ServiceConnection connection = new android.content.ServiceConnection() {
        @Override public void onServiceConnected(android.content.ComponentName name, android.os.IBinder binder) {
            remote = IWifiControl.Stub.asInterface(binder);
        }
        @Override public void onServiceDisconnected(android.content.ComponentName name) { remote = null; }
    };
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("float","Wi-Fi 플로팅",NotificationManager.IMPORTANCE_LOW));
        Notification notification=new Notification.Builder(this,"float").setContentTitle("WiFi Float 실행 중").setContentText("길게 눌러 버튼 종료")
            .setSmallIcon(android.R.drawable.stat_notify_sync).setContentIntent(PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE)).build();
        startForeground(1,notification);
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        bubble=new TextView(this); bubble.setText("Wi-Fi"); bubble.setTextColor(Color.WHITE); bubble.setTextSize(15); bubble.setGravity(Gravity.CENTER);
        GradientDrawable bg=new GradientDrawable(); bg.setColor(0xE51D70E8); bg.setCornerRadius(100); bubble.setBackground(bg); bubble.setElevation(12);
        int size=(int)(64*getResources().getDisplayMetrics().density);
        params=new WindowManager.LayoutParams(size,size,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.LEFT; params.x=20; params.y=250;
        bubble.setOnTouchListener(new View.OnTouchListener(){ float downX,downY; int x,y; long downTime; boolean moved;
            @Override public boolean onTouch(View v,android.view.MotionEvent e){ switch(e.getActionMasked()){
                case MotionEvent.ACTION_DOWN: downX=e.getRawX();downY=e.getRawY();x=params.x;y=params.y;downTime=System.currentTimeMillis();moved=false;return true;
                case MotionEvent.ACTION_MOVE: if(Math.abs(e.getRawX()-downX)>12||Math.abs(e.getRawY()-downY)>12)moved=true; if(moved){params.x=x+(int)(e.getRawX()-downX);params.y=y+(int)(e.getRawY()-downY);wm.updateViewLayout(bubble,params);}return true;
                case MotionEvent.ACTION_UP: if(!moved){if(System.currentTimeMillis()-downTime>850)stopSelf();else toggleWifi();}return true;
            } return true; }
        });
        wm.addView(bubble,params);
        try { Shizuku.bindUserService(serviceArgs, connection); } catch(Throwable e) { toast("Shizuku 서비스 연결 실패: "+e.getMessage()); }
    }
    private void toggleWifi(){
        if(busy)return;
        if(!Shizuku.pingBinder()||Shizuku.checkSelfPermission()!=android.content.pm.PackageManager.PERMISSION_GRANTED){ toast("Shizuku 연결/권한을 확인해줘");return; }
        WifiManager wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        boolean currentlyOn=wifi.isWifiEnabled();
        boolean enable=!currentlyOn;
        busy=true; bubble.setText("···");
        new Thread(()->{
            String error=null;
            try {
                IWifiControl control = remote;
                if (control == null) throw new IllegalStateException("Shizuku 서비스 연결 중입니다. 잠시 후 다시 눌러주세요.");
                int result = control.setWifiEnabled(enable);
                if (result != 0) error = "Wi-Fi 명령 실패 (종료 코드 " + result + ")";
            } catch(Exception ex){error="실행 실패: "+ex.getMessage();}
            final String msg=error;
            handler.post(()->{busy=false;bubble.setText("Wi-Fi");if(msg!=null)toast(msg);else toast(enable?"Wi-Fi 켜기 요청 완료":"Wi-Fi 끄기 요청 완료");});
        }).start();
    }
    private void toast(String s){handler.post(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
    @Override public void onDestroy(){if(bubble!=null&&wm!=null){wm.removeView(bubble);bubble=null;}try {Shizuku.unbindUserService(serviceArgs,connection,true);}catch(Throwable ignored){}super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}