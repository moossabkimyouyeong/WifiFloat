package com.lagpulse.app;
import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.provider.Settings;import android.graphics.Color;import android.view.*;import android.widget.*;
public class MainActivity extends Activity {
  SeekBar duration, intensity; TextView dText,iText;
  public void onCreate(Bundle b){super.onCreate(b);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(32,40,32,24);root.setBackgroundColor(Color.rgb(18,22,31));
    TextView title=label("LagPulse",28);root.addView(title);root.addView(label("게임 위에 표시되는 순간 CPU 부하 버튼",15));
    dText=label("",18);root.addView(dText);duration=new SeekBar(this);duration.setMax(3);duration.setProgress(1);root.addView(duration);
    iText=label("",18);root.addView(iText);intensity=new SeekBar(this);intensity.setMax(4);intensity.setProgress(1);root.addView(intensity);
    duration.setOnSeekBarChangeListener(listener());intensity.setOnSeekBarChangeListener(listener());refresh();
    Button start=new Button(this);start.setText("플로팅 버튼 시작");root.addView(start);start.setOnClickListener(v->start());
    Button stop=new Button(this);stop.setText("플로팅 버튼 종료");root.addView(stop);stop.setOnClickListener(v->stopService(new Intent(this,PulseService.class)));
    root.addView(label("최대 3초 후 자동 중지 · 과열 시 차단 · 게임에 따라 효과 없음",13));
    setContentView(root);
  }
  TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(0,10,0,10);return t;}
  SeekBar.OnSeekBarChangeListener listener(){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean user){refresh();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}};}
  void refresh(){if(dText!=null)dText.setText("지속시간: "+new String[]{"0.5","1","2","3"}[duration.getProgress()]+"초");if(iText!=null)iText.setText("강도: "+(intensity.getProgress()+1)+"/5");}
  void start(){if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));Toast.makeText(this,"다른 앱 위에 표시를 허용한 뒤 다시 눌러줘",Toast.LENGTH_LONG).show();return;}
    Intent i=new Intent(this,PulseService.class);i.putExtra("ms",new int[]{500,1000,2000,3000}[duration.getProgress()]);i.putExtra("level",intensity.getProgress()+1);startService(i);Toast.makeText(this,"플로팅 버튼 시작",Toast.LENGTH_SHORT).show();}
}