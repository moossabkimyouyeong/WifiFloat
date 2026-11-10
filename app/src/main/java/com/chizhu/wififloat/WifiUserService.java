package com.chizhu.wififloat;

import android.os.RemoteException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class WifiUserService extends IWifiControl.Stub {
    @Override public int setWifiEnabled(boolean enabled) throws RemoteException {
        try {
            java.lang.Process p = new ProcessBuilder("sh", "-c", enabled ? "svc wifi enable" : "svc wifi disable").redirectErrorStream(true).start();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = p.getInputStream();
            byte[] buf = new byte[1024]; int n;
            while ((n=in.read(buf))!=-1 && out.size()<8192) out.write(buf,0,n);
            int exit = p.waitFor();
            p.destroy();
            return exit;
        } catch (Exception e) { throw new RemoteException(e.toString()); }
    }
}