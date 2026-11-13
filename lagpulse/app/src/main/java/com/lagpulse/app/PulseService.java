package com.lagpulse.app;
import android.app.*;import android.content.*;import android.graphics.Color;import android.graphics.PixelFormat;import android.os.*;import android.view.*;import android.widget.*;import java.util.concurrent.atomic.AtomicBoolean;
public class PulseService extends Service {
 WindowManager wm;TextView button;AtomicBoolean busy=new AtomicBoolean(false);volatile boolean running=false;int ms=1000,level=2;long last=0; Handler safetyHandler=new Handler(Looper.getMainLooper());
 public IBinder onBind(Intent i){return null;}
 public int onStartCommand(Intent i,int flags,int id){if(i!=null){ms=i.getIntExtra("ms",1000);level=i.getIntExtra("level",2);}if(button==null)show();return START_NOT_STICKY;}
 void show(){wm=(WindowManager)getSystemService(WINDOW_SERVICE);button=new TextView(this);button.setText("PULSE");button.setTextColor(Color.WHITE);button.setTextSize(14);button.setGravity(Gravity.CENTER);button.setBackgroundColor(Color.rgb(65,90,180));WindowManager.LayoutParams p=new WindowManager.LayoutParams(155,105,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.LEFT;p.x=40;p.y=250;wm.addView(button,p);
 button.setOnClickListener(v->pulse());button.setOnTouchListener(new View.OnTouchListener(){float x,y;int ox,oy;boolean moved;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==0){x=e.getRawX();y=e.getRawY();ox=p.x;oy=p.y;moved=false;return true;}if(e.getAction()==1){if(!moved)v.performClick();return true;}if(e.getAction()==2){int dx=(int)(e.getRawX()-x),dy=(int)(e.getRawY()-y);if(Math.abs(dx)+Math.abs(dy)>12)moved=true;if(moved){p.x=ox+dx;p.y=oy+dy;wm.updateViewLayout(button,p);}return true;}return false;}});
 }
 void pulse(){long now=SystemClock.elapsedRealtime();if(now-last<6000||!busy.compareAndSet(false,true))return;last=now;
 PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);if(Build.VERSION.SDK_INT>=29&&pm.getCurrentThermalStatus()>=PowerManager.THERMAL_STATUS_MODERATE){busy.set(false);Toast.makeText(this,"기기가 뜨거워 실행을 차단했어",Toast.LENGTH_SHORT).show();return;}
 IntentFilter f=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);Intent battery=registerReceiver(null,f);if(battery!=null&&battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)>=420){busy.set(false);Toast.makeText(this,"배터리 온도가 높아 중단했어",Toast.LENGTH_SHORT).show();return;}
 button.setText("RUN");button.setBackgroundColor(Color.rgb(170,75,50));running=true;long end=SystemClock.elapsedRealtime()+Math.min(ms,3000);
 int cores=Runtime.getRuntime().availableProcessors(); int threads=Math.min(cores,Math.max(1,(int)Math.ceil(cores*(level/5.0))));
 // Cap intensive mode to one second and periodically monitor thermal state.
 end=Math.min(end,SystemClock.elapsedRealtime()+1000); final long stopAt=end;
 safetyHandler.postDelayed(new Runnable(){public void run(){if(!running)return;PowerManager p=(PowerManager)getSystemService(POWER_SERVICE);if(Build.VERSION.SDK_INT>=29&&p.getCurrentThermalStatus()>=PowerManager.THERMAL_STATUS_MODERATE){running=false;return;}safetyHandler.postDelayed(this,100);}},100);
 for(int j=0;j<threads;j++)new Thread(()->{double a=1.234;while(running&&SystemClock.elapsedRealtime()<stopAt){for(int k=0;k<50000&&running;k++)a=Math.sin(a+0.001)*Math.cos(a+0.002)+1.1;}}, "pulse-worker").start();
 new Handler(Looper.getMainLooper()).postDelayed(()->{running=false;busy.set(false);if(button!=null){button.setText("PULSE");button.setBackgroundColor(Color.rgb(65,90,180));}},Math.min(ms,1000));
 }
 public void onDestroy(){running=false;if(button!=null&&wm!=null){wm.removeView(button);button=null;}super.onDestroy();}
}