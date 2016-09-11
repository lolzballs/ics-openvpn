package de.blinkt.openvpn.core.tunnel;

import android.util.Log;

import java.io.IOException;
import java.lang.Runnable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.blinkt.openvpn.core.OpenVPNService;

public class SSLTunnelThread extends Thread {

    public ServerSocket mTunnel;

    private OpenVPNService mService;
    private int mPort;
    private String mRIP;
    private int mRPort;

    public SSLTunnelThread(OpenVPNService service, int port, String rIP, int rPort) {
        mService = service;
        mPort = port;
        mRIP = rIP;
        mRPort = rPort;
        try {
            mTunnel = new ServerSocket(mPort, 50, InetAddress.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setName("SSLTunnelThread");
    }

    public void stopTunnel() {
        // TODO: Stop tunnel
    }

    public final SSLSocketFactory getFactory() {
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (isInterrupted()) {
                    mTunnel.close();
                    return;
                }

                Socket client;
                try {
                    client = mTunnel.accept();
                } catch (SocketException e) {
                    return;
                }

                Socket server = getFactory().createSocket(mRIP, mRPort);
                ((SSLSocket) server).startHandshake();

                new Relay(client.getInputStream(), server.getOutputStream()).start();
                new Relay(server.getInputStream(), client.getOutputStream()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
