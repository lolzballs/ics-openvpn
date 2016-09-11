package de.blinkt.openvpn.core.tunnel;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class Relay extends Thread {
    private static final int BUFFER_SIZE = 4096;

    private InputStream mIn;
    private OutputStream mOut;

    public Relay(InputStream in, OutputStream out) {
        mIn = in;
        mOut = out;
    }

    @Override
    public void run() {
        int n = 0;
        byte[] buf = new byte[BUFFER_SIZE];

        try {
            while ((n = mIn.read(buf)) > 0) {
                if (Thread.interrupted()) {
                    Log.d("SSLTunnelRelay", "Interrupted");
                    try {
                        mIn.close();
                        mOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                mOut.write(buf, 0, n);
                mOut.flush();

                for (int i = 0; i < n; i++) {
                    if (buf[i] == 7)
                        buf[i] = '#';
                }
            }
        } catch (SocketException e) {
            Log.i("SSLTunnelRelay", "Socket exception occured, stopping relay...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mIn.close();
                mOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
