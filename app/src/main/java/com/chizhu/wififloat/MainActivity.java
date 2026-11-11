package com.chizhu.wififloat;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.Settings;import android.graphics.Color;import android.view.*;import android.widget.*;import rikka.shizuku.Shizuku;
public class MainActivity extends Activity {
 private LinearLayout layout;private TextView status;private android.content.SharedPreferences prefs;
 private void button(String s,View.OnClickListener c){Button b=new Button(this);b.setText(s);b.setOnClickListener(c);layout.addView(b);}
 private EditText number(String label,int initial){TextView t=new TextView(this);t.setText(label);layout.addView(t);EditText e=new EditText(this);e.setInputType(2);e.setText(""+initial);layout.addView(e);return e;}
 private int parse(EditText e,int fallback){try{return Integer.parseInt(e.getText().toString());}catch(Exception ex){return fallback;}}
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("macro",0);ScrollView scroll=new ScrollView(this);layout=new LinearLayout(this);layout.setOrientation(1);layout.setPadding(32,32,32,60);scroll.addView(layout);setContentView(scroll);
 TextView title=new TextView(this);title.setText("WiFi Float  |  Macro");title.setTextSize(25);title.setTextColor(Color.BLACK);layout.addView(title);
 status=new TextView(this);layout.addView(status);
 button("Shizuku 권한 요청",v->{try{if(Shizuku.pingBinder())Shizuku.requestPermission(101);else Toast.makeText(this,"Shizuku를 먼저 실행해줘",1).show();}catch(Throwable e){Toast.makeText(this,"Shizuku 연결 실패",1).show();}});
 button("오버레이 권한",v->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))));
 button("터치 접근성 권한",v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
 button("플로팅 버튼 시작",v->{if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"오버레이 권한 필요",1).show();return;}startForegroundService(new Intent(this,FloatService.class));});
 button("플로팅 버튼 종료",v->stopService(new Intent(this,FloatService.class)));
 TextView section=new TextView(this);section.setText("매크로 설정");section.setTextSize(21);layout.addView(section);
 CheckBox touch=new CheckBox(this);touch.setText("터치 매크로");touch.setChecked(prefs.getBoolean("touch",true));layout.addView(touch);
 CheckBox wifi=new CheckBox(this);wifi.setText("와이파이 ON/OFF 반복");wifi.setChecked(prefs.getBoolean("wifi",false));layout.addView(wifi);
 TextView point=new TextView(this);point.setText("터치 위치: "+prefs.getInt("x",500)+", "+prefs.getInt("y",900));layout.addView(point);
 button("화면에서 직접 터치 위치 지정",v->{if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"오버레이 권한 필요",1).show();return;}Intent i=new Intent(this,FloatService.class);i.setAction("PICK");startForegroundService(i);moveTaskToBack(true);});
 EditText delay=number("반복 간격 (밀리초, 최소 150)",prefs.getInt("delay",1000));
 EditText repeat=number("반복 횟수 (0 = 무한)",prefs.getInt("repeat",10));
 TextView opacityLabel=new TextView(this);opacityLabel.setText("플로팅 버튼 불투명도");layout.addView(opacityLabel);
 SeekBar opacity=new SeekBar(this);opacity.setMax(90);opacity.setProgress(prefs.getInt("opacity",100)-10);layout.addView(opacity);
 TextView opacityText=new TextView(this);opacityText.setText((opacity.getProgress()+10)+"%");layout.addView(opacityText);
 opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean u){opacityText.setText((p+10)+"%");prefs.edit().putInt("opacity",p+10).apply();sendBroadcast(new Intent("com.chizhu.wififloat.REFRESH").setPackage(getPackageName()));}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
 button("설정 저장",v->{prefs.edit().putBoolean("touch",touch.isChecked()).putBoolean("wifi",wifi.isChecked()).putInt("delay",Math.max(150,parse(delay,1000))).putInt("repeat",Math.max(0,parse(repeat,10))).apply();Toast.makeText(this,"저장 완료",0).show();});
 button("매크로 실행",v->{prefs.edit().putBoolean("touch",touch.isChecked()).putBoolean("wifi",wifi.isChecked()).putInt("delay",Math.max(150,parse(delay,1000))).putInt("repeat",Math.max(0,parse(repeat,10))).apply();Intent i=new Intent(this,FloatService.class);i.setAction("START");startForegroundService(i);});
 button("매크로 중지",v->{Intent i=new Intent(this,FloatService.class);i.setAction("STOP");startService(i);});
 }
 @Override protected void onResume(){super.onResume();if(status!=null)status.setText("터치 위치: "+prefs.getInt("x",500)+", "+prefs.getInt("y",900)+"\n접근성: "+(TouchService.active!=null?"연결됨":"설정 필요"));}
}