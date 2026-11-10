package com.chizhu.wififloat;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    private TextView status;
    private final Shizuku.OnRequestPermissionResultListener listener = (requestCode, grantResult) -> runOnUiThread(this::updateStatus);
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Shizuku.addRequestPermissionResultListener(listener);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(45,65,45,30);
        TextView title = new TextView(this); title.setText("WiFi Float"); title.setTextSize(28); title.setTextColor(Color.BLACK); layout.addView(title);
        status = new TextView(this); status.setPadding(0,30,0,25); layout.addView(status);
        addButton(layout,"1. Shizuku 권한 요청", v -> requestShizuku());
        addButton(layout,"2. 오버레이 권한 허용", v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())); startActivity(i);
        });
        addButton(layout,"3. 플로팅 버튼 시작", v -> {
            if (!Settings.canDrawOverlays(this)) { Toast.makeText(this,"오버레이 권한을 먼저 허용해줘",Toast.LENGTH_LONG).show(); return; }
            if (!shizukuReady()) { Toast.makeText(this,"Shizuku를 실행하고 권한을 승인해줘",Toast.LENGTH_LONG).show(); return; }
            startForegroundService(new Intent(this,FloatService.class));
            Toast.makeText(this,"게임으로 이동하면 버튼이 떠 있어",Toast.LENGTH_LONG).show();
        });
        addButton(layout,"4. 플로팅 버튼 종료", v -> stopService(new Intent(this,FloatService.class)));
        TextView note = new TextView(this); note.setText("버튼: 짧게 누르면 Wi-Fi 전환, 드래그하면 이동. 길게 누르면 종료.\nShizuku를 먼저 실행해야 해. 일부 Android 버전에서 명령이 차단될 수 있어."); note.setPadding(0,25,0,0); layout.addView(note);
        setContentView(layout); updateStatus();
    }
    private void addButton(LinearLayout layout, String name, View.OnClickListener click) { Button b=new Button(this); b.setText(name); b.setOnClickListener(click); layout.addView(b); }
    private boolean shizukuReady() { try { return Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED; } catch (Throwable e) { return false; } }
    private void requestShizuku() { try {
        if (!Shizuku.pingBinder()) { Toast.makeText(this,"Shizuku 앱을 먼저 실행해줘",Toast.LENGTH_LONG).show(); return; }
        if (Shizuku.checkSelfPermission()!=android.content.pm.PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(101);
        else updateStatus();
    } catch (Throwable e) { Toast.makeText(this,"Shizuku 연결 실패: "+e.getMessage(),Toast.LENGTH_LONG).show(); } }
    private void updateStatus() { if(status!=null) status.setText("Shizuku: " + (shizukuReady()?"연결됨 / 권한 허용":"연결 또는 권한 필요") + "\n오버레이: " + (Settings.canDrawOverlays(this)?"허용":"권한 필요")); }
    @Override protected void onResume(){ super.onResume(); updateStatus(); }
    @Override protected void onDestroy(){ Shizuku.removeRequestPermissionResultListener(listener); super.onDestroy(); }
}