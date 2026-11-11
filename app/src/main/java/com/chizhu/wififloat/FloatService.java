package com.chizhu.wififloat;
import android.app.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.*;import android.net.wifi.WifiManager;import android.os.*;import android.view.*;import android.widget.*;import rikka.shizuku.Shizuku;
public class FloatService extends Service {
 private WindowManager wm;private TextView bubble;private WindowManager.LayoutParams params;private View picker, panel;private final Handler h=new Handler(Looper.getMainLooper());private boolean running=false;private int iterations=0;private Runnable task;private IWifiControl remote;
 private final Shizuku.UserServiceArgs args=new Shizuku.UserServiceArgs(new ComponentName("com.chizhu.wififloat",WifiUserService.class.getName())).daemon(false).processNameSuffix("wifi").debuggable(false).version(1);
 private final ServiceConnection connection=new ServiceConnection(){public void onServiceConnected(ComponentName n,IBinder b){remote=IWifiControl.Stub.asInterface(b);}public void onServiceDisconnected(ComponentName n){remote=null;}};
 private android.content.SharedPreferences prefs(){return getSharedPreferences("macro",0);}
 private void toast(String s){h.post(()->Toast.makeText(this,s,Toast.LENGTH_SHORT).show());}
 @Override public void onCreate(){super.onCreate();NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("float","WiFi Float",NotificationManager.IMPORTANCE_LOW));startForeground(1,new Notification.Builder(this,"float").setContentTitle("WiFi Float 실행 중").setSmallIcon(android.R.drawable.stat_notify_sync).build());wm=(WindowManager)getSystemService(WINDOW_SERVICE);
 bubble=new TextView(this);bubble.setText("WiFi");bubble.setTextColor(Color.WHITE);bubble.setGravity(Gravity.CENTER);bubble.setTextSize(12);GradientDrawable bg=new GradientDrawable();bg.setColor(0xFF2467DC);bg.setCornerRadius(100);bubble.setBackground(bg);bubble.setElevation(8);bubble.setAlpha(prefs().getInt("opacity",100)/100f);
 int size=(int)(49*getResources().getDisplayMetrics().density);params=new WindowManager.LayoutParams(size,size,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);params.gravity=Gravity.TOP|Gravity.LEFT;params.x=18;params.y=260;
 bubble.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int px,py;long down;boolean moved;public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:sx=e.getRawX();sy=e.getRawY();px=params.x;py=params.y;down=System.currentTimeMillis();moved=false;return true;case MotionEvent.ACTION_MOVE:if(Math.abs(e.getRawX()-sx)>12||Math.abs(e.getRawY()-sy)>12)moved=true;if(moved){params.x=px+(int)(e.getRawX()-sx);params.y=py+(int)(e.getRawY()-sy);wm.updateViewLayout(bubble,params);}return true;case MotionEvent.ACTION_UP:if(!moved){if(System.currentTimeMillis()-down>750)toggleWifi();else showMenu();}return true;}return true;}});
 wm.addView(bubble,params);try{Shizuku.bindUserService(args,connection);}catch(Throwable e){toast("Shizuku 연결 실패");}
 }
 private void closePanel(){if(panel!=null){wm.removeView(panel);panel=null;}}
 private void showMenu(){
  if(panel!=null){closePanel();return;}
  android.content.SharedPreferences p=prefs();
  ScrollView scroll=new ScrollView(this);
  LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(24,18,24,18);l.setBackgroundColor(0xFF182335);scroll.addView(l);
  TextView title=new TextView(this);title.setText("WiFi Float  •  매크로");title.setTextColor(Color.WHITE);title.setTextSize(20);l.addView(title);
  CheckBox touch=new CheckBox(this);touch.setText("터치 매크로");touch.setTextColor(Color.WHITE);touch.setChecked(p.getBoolean("touch",true));l.addView(touch);
  CheckBox wifi=new CheckBox(this);wifi.setText("와이파이 반복 전환");wifi.setTextColor(Color.WHITE);wifi.setChecked(p.getBoolean("wifi",false));l.addView(wifi);
  TextView pos=new TextView(this);pos.setText("터치 위치: "+p.getInt("x",500)+", "+p.getInt("y",900));pos.setTextColor(Color.WHITE);l.addView(pos);
  Button pickButton=new Button(this);pickButton.setText("화면 터치로 위치 지정");l.addView(pickButton);
  TextView delayLabel=new TextView(this);delayLabel.setText("반복 간격 (ms)");delayLabel.setTextColor(Color.WHITE);l.addView(delayLabel);
  EditText delay=new EditText(this);delay.setInputType(2);delay.setSingleLine(true);delay.setText(""+p.getInt("delay",1000));delay.setTextColor(Color.WHITE);l.addView(delay);
  TextView repeatLabel=new TextView(this);repeatLabel.setText("반복 횟수 (0 = 무한)");repeatLabel.setTextColor(Color.WHITE);l.addView(repeatLabel);
  EditText repeat=new EditText(this);repeat.setInputType(2);repeat.setSingleLine(true);repeat.setText(""+p.getInt("repeat",10));repeat.setTextColor(Color.WHITE);l.addView(repeat);
  TextView opacityLabel=new TextView(this);opacityLabel.setText("버튼 불투명도: "+p.getInt("opacity",100)+"%");opacityLabel.setTextColor(Color.WHITE);l.addView(opacityLabel);
  SeekBar opacity=new SeekBar(this);opacity.setMax(90);opacity.setProgress(p.getInt("opacity",100)-10);l.addView(opacity);
  opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar bar,int progress,boolean user){int value=progress+10;opacityLabel.setText("버튼 불투명도: "+value+"%");bubble.setAlpha(value/100f);}public void onStartTrackingTouch(SeekBar bar){}public void onStopTrackingTouch(SeekBar bar){}});
  Runnable save=()->{int d=1000,r=10;try{d=Integer.parseInt(delay.getText().toString());}catch(Exception ignored){}try{r=Integer.parseInt(repeat.getText().toString());}catch(Exception ignored){}p.edit().putBoolean("touch",touch.isChecked()).putBoolean("wifi",wifi.isChecked()).putInt("delay",Math.max(150,d)).putInt("repeat",Math.max(0,r)).putInt("opacity",opacity.getProgress()+10).apply();};
  pickButton.setOnClickListener(v->{save.run();closePanel();pick();});
  Button saveButton=new Button(this);saveButton.setText("설정 저장");saveButton.setOnClickListener(v->{save.run();toast("매크로 설정 저장 완료");closePanel();});l.addView(saveButton);
  Button start=new Button(this);start.setText("▶ 매크로 실행");start.setOnClickListener(v->{save.run();closePanel();startMacro();});l.addView(start);
  Button stop=new Button(this);stop.setText("■ 매크로 중지");stop.setOnClickListener(v->{stopMacro();closePanel();});l.addView(stop);
  Button wifiButton=new Button(this);wifiButton.setText("Wi-Fi 한 번 전환");wifiButton.setOnClickListener(v->toggleWifi());l.addView(wifiButton);
  Button close=new Button(this);close.setText("닫기");close.setOnClickListener(v->closePanel());l.addView(close);
  WindowManager.LayoutParams wp=new WindowManager.LayoutParams((int)(300*getResources().getDisplayMetrics().density),-2,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_DIM_BEHIND,PixelFormat.TRANSLUCENT);
  wp.gravity=Gravity.CENTER;wp.dimAmount=0.25f;wp.softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
  panel=scroll;wm.addView(panel,wp);
 }
 private void pick(){if(picker!=null)return;stopMacro();bubble.setVisibility(View.GONE);FrameLayout layer=new FrameLayout(this);layer.setBackgroundColor(0x44000000);TextView hint=new TextView(this);hint.setText("원하는 화면 위치를 터치하세요");hint.setTextColor(Color.WHITE);hint.setTextSize(20);hint.setGravity(Gravity.CENTER);hint.setBackgroundColor(0xCC182333);FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,(int)(70*getResources().getDisplayMetrics().density),Gravity.TOP);layer.addView(hint,hp);
 WindowManager.LayoutParams p=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.LEFT;
 layer.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_UP){prefs().edit().putInt("x",(int)e.getRawX()).putInt("y",(int)e.getRawY()).apply();wm.removeView(layer);picker=null;bubble.setVisibility(View.VISIBLE);toast("터치 위치 저장 완료");}return true;});picker=layer;wm.addView(layer,p);}
 private void toggleWifi(){if(remote==null){toast("Shizuku 연결을 확인해줘");return;}new Thread(()->{try{WifiManager wifi=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);remote.setWifiEnabled(!wifi.isWifiEnabled());}catch(Exception e){toast("WiFi 명령 실패");}}).start();}
 private void stopMacro(){running=false;if(task!=null)h.removeCallbacks(task);task=null;if(bubble!=null)bubble.setText("WiFi");}
 private void startMacro(){stopMacro();boolean touch=prefs().getBoolean("touch",true),wifi=prefs().getBoolean("wifi",false);if(!touch&&!wifi){toast("매크로 동작을 선택해줘");return;}if(touch&&TouchService.active==null){toast("접근성에서 터치 서비스 활성화 필요");return;}if(wifi&&remote==null){toast("Shizuku 연결 필요");return;}running=true;iterations=0;bubble.setText("STOP");final Runnable[] loop=new Runnable[1];loop[0]=()->{if(!running)return;int repeat=prefs().getInt("repeat",10);if(repeat>0&&iterations>=repeat){stopMacro();return;}iterations++;if(touch&&TouchService.active!=null)TouchService.active.tap(prefs().getInt("x",500),prefs().getInt("y",900));if(wifi)toggleWifi();task=loop[0];h.postDelayed(task,Math.max(150,prefs().getInt("delay",1000)));};loop[0].run();}
 @Override public int onStartCommand(Intent i,int flags,int id){if(i!=null){String a=i.getAction();if("PICK".equals(a))h.postDelayed(this::pick,500);else if("START".equals(a))startMacro();else if("STOP".equals(a))stopMacro();}if(bubble!=null)bubble.setAlpha(prefs().getInt("opacity",100)/100f);return START_STICKY;}
 @Override public void onDestroy(){stopMacro();if(picker!=null)wm.removeView(picker);closePanel();if(bubble!=null)wm.removeView(bubble);try{Shizuku.unbindUserService(args,connection,true);}catch(Throwable e){}super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}